package net.pantasystem.milktea.data.infrastructure.user.renote.mute.delegate

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.IsSupportRenoteMuteInstance
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.RenoteMuteApiAdapter
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.db.RenoteMuteDao
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.db.RenoteMuteRecord
import net.pantasystem.milktea.model.account.GetAccount
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.renote.mute.RenoteMute
import javax.inject.Inject

internal open class CreateRenoteMuteAndPushToRemoteDelegate @Inject constructor(
    private val getAccount: GetAccount,
    private val renoteMuteDao: RenoteMuteDao,
    private val findRenoteMuteAndUpdateMemCache: FindRenoteMuteAndUpdateMemCacheDelegate,
    private val isSupportRenoteMuteInstance: IsSupportRenoteMuteInstance,
    private val renoteMuteApiAdapter: RenoteMuteApiAdapter,
    @IODispatcher private val coroutineDispatcher: CoroutineDispatcher
) {
    open suspend operator fun invoke(userId: User.Id) = runCancellableCatching {
        withContext(coroutineDispatcher) {
            val account = getAccount.get(userId.accountId)
            val isNeedPush = when (val exists = findRenoteMuteAndUpdateMemCache(userId).getOrNull()) {
                null -> {
                    renoteMuteDao.insert(
                        RenoteMuteRecord.from(
                            RenoteMute(
                                userId,
                                createdAt = Clock.System.now(),
                                postedAt = null,
                            )
                        )
                    )
                    true
                }
                else -> {
                    exists.postedAt == null
                }
            }
            val created = findRenoteMuteAndUpdateMemCache(userId).getOrThrow()
            if (isNeedPush && isSupportRenoteMuteInstance(account.accountId)) {
                renoteMuteApiAdapter.create(
                    userId
                )
                // APIへの送信に成功したのでpostedAtに成功した日時を記録する
                renoteMuteDao.update(
                    RenoteMuteRecord.from(
                        created.copy(
                            postedAt = Clock.System.now()
                        )
                    )
                )
            }

            findRenoteMuteAndUpdateMemCache(userId).getOrThrow()
        }
    }
}