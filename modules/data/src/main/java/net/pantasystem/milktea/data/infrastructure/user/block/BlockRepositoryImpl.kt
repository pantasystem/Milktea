package net.pantasystem.milktea.data.infrastructure.user.block

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.infrastructure.toUserRelated
import net.pantasystem.milktea.data.infrastructure.user.UserActionResult
import net.pantasystem.milktea.data.infrastructure.user.UserApiAdapter
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserRepository
import net.pantasystem.milktea.model.user.block.BlockRepository
import javax.inject.Inject

internal class BlockRepositoryImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val userApiAdapter: UserApiAdapter,
    private val userDataSource: UserDataSource,
    @IODispatcher private val coroutineDispatcher: CoroutineDispatcher,
    loggerFactory: Logger.Factory
) : BlockRepository {

    private val logger by lazy {
        loggerFactory.create("BlockRepositoryImpl")
    }

    override suspend fun create(userId: User.Id): Result<Unit> {
        return runCancellableCatching {
            withContext(coroutineDispatcher) {
                updateCacheFrom(userId, userApiAdapter.blockUser(userId)) { user ->
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
                updateCacheFrom(userId, userApiAdapter.unblockUser(userId)) { user ->
                    user.copy(
                        related = user.related?.copy(isBlocking = false)
                    )
                }
            }
        }.onFailure {
            logger.error("unblock failed", it)
        }
    }

    private suspend fun updateCacheFrom(userId: User.Id, result: UserActionResult, reducer: suspend (User.Detail) -> User.Detail) {
        val user = userRepository.find(userId, true) as User.Detail
        val updated = when(result) {
            is UserActionResult.Mastodon -> {
                user.copy(
                    related = result.relationship.toUserRelated()
                )
            }
            UserActionResult.Misskey -> {
                reducer(user)
            }
        }
        userDataSource.add(updated)
    }
}