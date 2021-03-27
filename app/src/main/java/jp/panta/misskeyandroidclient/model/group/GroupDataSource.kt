package jp.panta.misskeyandroidclient.model.group

import jp.panta.misskeyandroidclient.model.AddResult

interface GroupDataSource {

    suspend fun find(groupId: Group.Id)

    suspend fun add(group: Group) : AddResult

    suspend fun addAll(groups: List<Group>) : List<AddResult>

    suspend fun delete(groupId: Group.Id) : Boolean
}