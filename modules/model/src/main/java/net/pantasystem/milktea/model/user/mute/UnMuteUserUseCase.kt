package net.pantasystem.milktea.model.user.mute

import net.pantasystem.milktea.common.flatMapCancellableCatching
import net.pantasystem.milktea.model.UseCase
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject

class UnMuteUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val muteRepository: MuteRepository,
) : UseCase {

    suspend operator fun invoke(userId: User.Id): Result<Unit> {
        return muteRepository.delete(userId).flatMapCancellableCatching {
            userRepository.sync(userId)
        }
    }
}