package net.pantasystem.milktea.model.group

import net.pantasystem.milktea.model.UseCase1
import javax.inject.Inject

class RejectGroupInvitationUseCase @Inject constructor(
    private val groupRepository: GroupRepository,
): UseCase1<InvitationId, Unit> {

    override suspend fun invoke(p1: InvitationId): Result<Unit> {
        return groupRepository.reject(p1)
    }
}