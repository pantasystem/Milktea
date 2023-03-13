package net.pantasystem.milktea.data.infrastructure.user.renote.mute.delegate

import kotlinx.coroutines.*
import net.pantasystem.milktea.api.misskey.users.renote.mute.RenoteMuteDTO
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.*
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.db.RenoteMuteDao
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.db.RenoteMuteRecord
import net.pantasystem.milktea.model.account.GetAccount
import javax.inject.Inject

internal interface SyncRenoteMuteDelegate {
    suspend operator fun invoke(accountId: Long): Result<Unit>
}

internal class SyncRenoteMuteDelegateImpl @Inject constructor(
    private val getAccount: GetAccount,
    private val cache: RenoteMuteCache,
    private val renoteMuteDao: RenoteMuteDao,
    private val createAndPushToRemote: CreateRenoteMuteAndPushToRemoteDelegate,
    private val unPushedRenoteMutesDiffFilter: UnPushedRenoteMutesDiffFilter,
    private val isSupportRenoteMuteInstance: IsSupportRenoteMuteInstance,
    private val findAllRemoteRenoteMutesDelegate: FindAllRemoteRenoteMutesDelegate,
    @IODispatcher private val coroutineDispatcher: CoroutineDispatcher
) : SyncRenoteMuteDelegate {
    override suspend fun invoke(accountId: Long): Result<Unit> = runCancellableCatching {
        withContext(coroutineDispatcher) {
            // NOTE: キャッシュを更新しておく
            renoteMuteDao.findByAccount(accountId).map {
                it.toModel()
            }.also {
                cache.clearBy(accountId)
                cache.addAll(it, true)
            }

            if (!isSupportRenoteMuteInstance(accountId)) {
                return@withContext
            }

            val account = getAccount.get(accountId)

            val mutes: List<RenoteMuteDTO> = findAllRemoteRenoteMutesDelegate(
                account,
            )

            val unPushedRenoteMutes = unPushedRenoteMutesDiffFilter(
                mutes,
                renoteMuteDao.findByUnPushed(account.accountId)
                    .map {
                        it.toModel()
                    }
            )

            coroutineScope {
                unPushedRenoteMutes.map {
                    async {
                        createAndPushToRemote(it.userId)
                    }
                }
            }.awaitAll()

            renoteMuteDao.deleteBy(account.accountId)
            cache.clearBy(account.accountId)

            // FIXME: remote < localの場合データの不整合が発生してしまう可能性がある
            val newModels = mutes.map {
                it.toModel(account.accountId)
            }
            renoteMuteDao.insertAll(
                newModels.map {
                    RenoteMuteRecord.from(it)
                }
            )
            cache.addAll(newModels, true)
        }
    }
}