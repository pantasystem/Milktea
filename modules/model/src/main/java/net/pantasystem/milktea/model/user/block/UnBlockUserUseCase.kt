package net.pantasystem.milktea.model.user.block

import net.pantasystem.milktea.common.mapCancellableCatching
import net.pantasystem.milktea.model.UseCase
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject

class UnBlockUserUseCase @Inject constructor(
    private val blockRepository: BlockRepository,
    private val userRepository: UserRepository,
) : UseCase {

    suspend operator fun invoke(userId: User.Id): Result<Unit> {
        return blockRepository.delete(userId).mapCancellableCatching {
            userRepository.sync(userId).getOrThrow()
        }
    }
}