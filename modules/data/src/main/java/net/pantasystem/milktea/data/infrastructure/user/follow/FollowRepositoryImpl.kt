package net.pantasystem.milktea.data.infrastructure.user.follow

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.infrastructure.user.UserApiAdapter
import net.pantasystem.milktea.data.infrastructure.user.UserCacheUpdaterFromUserActionResult
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserRepository
import net.pantasystem.milktea.model.user.follow.FollowRepository
import javax.inject.Inject

internal class FollowRepositoryImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val userApiAdapter: UserApiAdapter,
    private val userCacheUpdaterFromUserActionResult: UserCacheUpdaterFromUserActionResult,
    @IODispatcher private val coroutineDispatcher: CoroutineDispatcher,
): FollowRepository {
    override suspend fun create(userId: User.Id): Result<Unit> = runCancellableCatching {
        withContext(coroutineDispatcher) {
            val user = userRepository.find(userId, true) as User.Detail
            val result = userApiAdapter.follow(userId)
            userCacheUpdaterFromUserActionResult(userId, result) { u ->
                u.copy(
                    related = (if (user.info.isLocked) user.related?.isFollowing else true)?.let {
                        (if (user.info.isLocked) true else user.related?.hasPendingFollowRequestFromYou)?.let { it1 ->
                            user.related?.copy(
                                isFollowing = it,
                                hasPendingFollowRequestFromYou = it1
                            )
                        }
                    }
                )
            }
        }
    }

    override suspend fun delete(userId: User.Id): Result<Unit> = runCancellableCatching {
        withContext(coroutineDispatcher) {
            val user = userRepository.find(userId, true) as User.Detail
            val result = if (user.info.isLocked) {
                userApiAdapter.cancelFollowRequest(userId)
            } else {
                userApiAdapter.unfollow(userId)
            }
            userCacheUpdaterFromUserActionResult(userId, result) { u ->
                u.copy(
                    related = u.related?.let{
                        it.copy(
                            isFollowing = if (u.info.isLocked) it.isFollowing else false,
                            hasPendingFollowRequestFromYou = if (u.info.isLocked) false else it.hasPendingFollowRequestFromYou
                        )
                    }
                )
            }
        }
    }
}