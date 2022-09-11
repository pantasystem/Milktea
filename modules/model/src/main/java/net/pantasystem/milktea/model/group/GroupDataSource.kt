package net.pantasystem.milktea.model.group

import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.model.AddResult

interface GroupDataSource {

    suspend fun find(groupId: Group.Id): Result<Group>

    suspend fun add(group: Group) : Result<AddResult>

    suspend fun addAll(groups: List<Group>) : Result<List<AddResult>>

    suspend fun delete(groupId: Group.Id) : Result<Boolean>

    fun observeOwnedGroups(accountId: Long) : Flow<List<GroupWithMember>>

    fun observeJoinedGroups(accountId: Long) : Flow<List<GroupWithMember>>

    fun observeOne(groupId: Group.Id) : Flow<GroupWithMember>
}