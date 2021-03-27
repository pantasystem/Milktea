package jp.panta.misskeyandroidclient.model.group

interface GroupRepository {

    suspend fun find(groupId: Group.Id) : Group

    suspend fun create(createGroup: CreateGroup) : Group

    suspend fun update(updateGroup: UpdateGroup) : Group

    suspend fun joined(accountId: Long) : List<Group>

    suspend fun owned(accountId: Long) : List<Group>

    suspend fun transfer(transfer: Transfer) : Group

    suspend fun pull(pull: Pull) : Group
}