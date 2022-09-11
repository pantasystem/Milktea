package net.pantasystem.milktea.model.group

interface GroupRepository {

    suspend fun syncOne(groupId: Group.Id) : Group

    suspend fun create(createGroup: CreateGroup) : Group

    suspend fun update(updateGroup: UpdateGroup) : Group

    suspend fun syncByJoined(accountId: Long) : List<Group>

    suspend fun syncByOwned(accountId: Long) : List<Group>

    suspend fun transfer(transfer: Transfer) : Group

    suspend fun pull(pull: Pull) : Group

    suspend fun invite(invite: Invite) : Result<Unit>

    suspend fun accept(invitationId: InvitationId) : Result<Unit>

    suspend fun reject(invitationId: InvitationId) : Result<Unit>
}