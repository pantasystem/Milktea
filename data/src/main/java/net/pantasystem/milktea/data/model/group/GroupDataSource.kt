package net.pantasystem.milktea.data.model.group

import net.pantasystem.milktea.data.model.AddResult

interface GroupDataSource {

    suspend fun find(groupId: Group.Id): Group

    suspend fun add(group: Group) : AddResult

    suspend fun addAll(groups: List<Group>) : List<AddResult>

    suspend fun delete(groupId: Group.Id) : Boolean
}