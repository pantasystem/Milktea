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
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.renote.mute.RenoteMute
import javax.inject.Inject

internal open class CreateRenoteMuteAndPushToRemoteDelegate @Inject constructor(
    private val accountRepository: AccountRepository,
    private val renoteMuteDao: RenoteMuteDao,
    private val findRenoteMuteAndUpdatememCache: FindRenoteMuteAndUpdateMemCacheDelegate,
    private val isSupportRenoteMuteInstance: IsSupportRenoteMuteInstance,
    private val renoteMuteApiAdapter: RenoteMuteApiAdapter,
    @IODispatcher private val coroutineDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(userId: User.Id) = runCancellableCatching {
        withContext(coroutineDispatcher) {
            val account = accountRepository.get(userId.accountId).getOrThrow()
            val isNeedPush = when (val exists = findRenoteMuteAndUpdatememCache(userId).getOrNull()) {
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
            val created = findRenoteMuteAndUpdatememCache(userId).getOrThrow()
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

            findRenoteMuteAndUpdatememCache(userId).getOrThrow()
        }
    }
}