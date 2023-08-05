package net.pantasystem.milktea.model.user.follow.requests

import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.UseCase
import net.pantasystem.milktea.model.user.FollowRequestRepository
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject

class AcceptFollowRequestUseCase @Inject constructor(
    private val followRequestRepository: FollowRequestRepository,
): UseCase {

    suspend operator fun invoke(userId: User.Id) = runCancellableCatching {
        followRequestRepository.accept(userId)
    }
}