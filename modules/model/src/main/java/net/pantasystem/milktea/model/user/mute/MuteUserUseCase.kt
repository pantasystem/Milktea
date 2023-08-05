package net.pantasystem.milktea.model.user.mute

import net.pantasystem.milktea.common.mapCancellableCatching
import net.pantasystem.milktea.model.UseCase
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject

class MuteUserUseCase @Inject constructor(
    private val muteRepository: MuteRepository,
    private val userRepository: UserRepository
): UseCase {

    suspend operator fun invoke(createMute: CreateMute): Result<Unit> {
        return muteRepository.create(createMute).mapCancellableCatching {
            userRepository.sync(createMute.userId).getOrThrow()
        }
    }
}