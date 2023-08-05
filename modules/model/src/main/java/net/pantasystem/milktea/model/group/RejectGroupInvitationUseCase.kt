package net.pantasystem.milktea.model.group

import net.pantasystem.milktea.model.UseCase
import javax.inject.Inject

class RejectGroupInvitationUseCase @Inject constructor(
    private val groupRepository: GroupRepository,
): UseCase {

    suspend operator fun invoke(p1: InvitationId): Result<Unit> {
        return groupRepository.reject(p1)
    }
}