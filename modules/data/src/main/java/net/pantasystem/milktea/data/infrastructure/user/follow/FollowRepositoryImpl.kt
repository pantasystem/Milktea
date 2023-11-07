package net.pantasystem.milktea.data.infrastructure.user.follow

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.infrastructure.user.UserCacheUpdaterFromUserActionResult
import net.pantasystem.milktea.model.user.FollowState
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserRepository
import net.pantasystem.milktea.model.user.follow.FollowRepository
import net.pantasystem.milktea.model.user.follow.FollowUpdateParams
import javax.inject.Inject

internal class FollowRepositoryImpl @Inject constructor(
    private val userRepository: UserRepository,
    private val followApiAdapter: FollowApiAdapter,
    private val userCacheUpdaterFromUserActionResult: UserCacheUpdaterFromUserActionResult,
    @IODispatcher private val coroutineDispatcher: CoroutineDispatcher,
): FollowRepository {
    override suspend fun create(userId: User.Id): Result<Unit> = runCancellableCatching {
        withContext(coroutineDispatcher) {
            val user = userRepository.find(userId, true) as User.Detail
            val result = followApiAdapter.follow(userId)
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
                followApiAdapter.cancelFollowRequest(userId)
            } else {
                followApiAdapter.unfollow(userId)
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

    override suspend fun update(userId: User.Id, params: FollowUpdateParams): Result<Unit> = runCancellableCatching {
        val user = userRepository.find(userId, true) as User.Detail
        if (user.followState != FollowState.FOLLOWING) {
            throw IllegalStateException("You can't update follow state of user who is not following.")
        }
        withContext(coroutineDispatcher) {
            val result = followApiAdapter.update(userId, params)
            userCacheUpdaterFromUserActionResult(userId, result) { u ->
                u.copy(
                    related = u.related?.copy(
                        isNotify = params.isNotify ?: false,
                    )
                )
            }
        }
    }
}