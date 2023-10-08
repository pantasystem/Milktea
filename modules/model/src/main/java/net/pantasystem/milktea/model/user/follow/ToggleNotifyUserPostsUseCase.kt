package net.pantasystem.milktea.model.user.follow

import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject

class ToggleNotifyUserPostsUseCase @Inject constructor(
    val followRepository: FollowRepository,
    val userRepository: UserRepository,
) {

    suspend operator fun invoke(userId: User.Id): Result<Unit> = runCancellableCatching {
        val user = userRepository.find(userId, detail = true) as User.Detail
        followRepository.update(
            userId, FollowUpdateParams(
                isNotify = user.related?.isNotify?.not()
            )
        ).getOrThrow()
        userRepository.sync(userId).getOrThrow()
    }
}