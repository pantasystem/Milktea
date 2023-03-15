package net.pantasystem.milktea.data.infrastructure.user.block

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.infrastructure.user.UserCacheUpdaterFromUserActionResult
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.block.BlockRepository
import javax.inject.Inject

internal class BlockRepositoryImpl @Inject constructor(
    private val blockApiAdapter: BlockApiAdapter,
    private val updateCacheFrom: UserCacheUpdaterFromUserActionResult,
    @IODispatcher private val coroutineDispatcher: CoroutineDispatcher,
    loggerFactory: Logger.Factory
) : BlockRepository {

    private val logger by lazy {
        loggerFactory.create("BlockRepositoryImpl")
    }

    override suspend fun create(userId: User.Id): Result<Unit> {
        return runCancellableCatching {
            withContext(coroutineDispatcher) {
                updateCacheFrom(userId, blockApiAdapter.blockUser(userId)) { user ->
                    user.copy(
                        related = user.related?.copy(
                            isBlocking = true
                        )
                    )
                }
            }
        }.onFailure {
            logger.error("block failed", it)
        }
    }

    override suspend fun delete(userId: User.Id): Result<Unit> {
        return runCancellableCatching {
            withContext(coroutineDispatcher) {
                updateCacheFrom(userId, blockApiAdapter.unblockUser(userId)) { user ->
                    user.copy(
                        related = user.related?.copy(isBlocking = false)
                    )
                }
            }
        }.onFailure {
            logger.error("unblock failed", it)
        }
    }

}