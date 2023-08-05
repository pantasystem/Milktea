package net.pantasystem.milktea.model.group

import net.pantasystem.milktea.model.UseCase
import javax.inject.Inject

class AcceptGroupInvitationUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) : UseCase {

    suspend operator fun invoke(invitationId: InvitationId): Result<Unit> {
        return groupRepository.accept(invitationId)
    }
}