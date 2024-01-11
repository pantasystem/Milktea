package net.pantasystem.milktea.model.user.block

import net.pantasystem.milktea.common.flatMapCancellableCatching
import net.pantasystem.milktea.model.UseCase
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject

class BlockUserUseCase @Inject constructor(
    private val blockRepository: BlockRepository,
    private val userRepository: UserRepository,
): UseCase {

    suspend operator fun invoke(userId: User.Id): Result<Unit> {
        return blockRepository.create(userId).flatMapCancellableCatching {
            userRepository.sync(userId)
        }
    }
}