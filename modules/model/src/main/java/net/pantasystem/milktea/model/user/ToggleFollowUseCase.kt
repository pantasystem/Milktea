package net.pantasystem.milktea.model.user

import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.UseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToggleFollowUseCase @Inject constructor(
    val userRepository: UserRepository,
) : UseCase {

    suspend operator fun invoke(userId: User.Id): Result<Unit> {
        return runCancellableCatching {
            val state = userRepository.find(userId, true) as User.Detail
            if (state.related.isFollowing) {
                userRepository.unfollow(userId)
            } else {
                userRepository.follow(userId)
            }
        }
    }
}