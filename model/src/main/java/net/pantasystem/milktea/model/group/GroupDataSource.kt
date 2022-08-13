package net.pantasystem.milktea.model.group

import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.model.AddResult

interface GroupDataSource {

    suspend fun find(groupId: Group.Id): Group

    suspend fun add(group: Group) : AddResult

    suspend fun addAll(groups: List<Group>) : List<AddResult>

    suspend fun delete(groupId: Group.Id) : Boolean

    fun observeOwnedGroups(accountId: Long) : Flow<List<GroupWithMember>>

    fun observeJoinedGroups(accountId: Long) : Flow<List<GroupWithMember>>
}