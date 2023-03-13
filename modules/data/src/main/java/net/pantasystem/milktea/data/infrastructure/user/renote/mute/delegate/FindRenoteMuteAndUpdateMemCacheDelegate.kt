package net.pantasystem.milktea.data.infrastructure.user.renote.mute.delegate

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.RenoteMuteCache
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.db.RenoteMuteDao
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.renote.mute.RenoteMute
import javax.inject.Inject

internal open class FindRenoteMuteAndUpdateMemCacheDelegate @Inject constructor(
    private val renoteMuteDao: RenoteMuteDao,
    private val cache: RenoteMuteCache,
    @IODispatcher private val coroutineDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(userId: User.Id): Result<RenoteMute> = runCancellableCatching {
        withContext(coroutineDispatcher) {
            // NOTE: APIにIdを指定して取得する仕組みがないのでローカルのみから取得する
            val result = renoteMuteDao.findByUser(userId.accountId, userId.id)?.toModel()
            if (result == null) {
                cache.remove(userId)
            } else {
                cache.add(result)
            }
            result?: throw NoSuchElementException("RenoteMuteは存在しません:$userId")
        }
    }
}

