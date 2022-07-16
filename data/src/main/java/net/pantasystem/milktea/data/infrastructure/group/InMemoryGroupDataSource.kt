package net.pantasystem.milktea.data.infrastructure.group

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.model.AddResult
import net.pantasystem.milktea.model.group.Group
import net.pantasystem.milktea.model.group.GroupDataSource
import net.pantasystem.milktea.model.group.GroupNotFoundException
import javax.inject.Inject

class InMemoryGroupDataSource @Inject constructor(): GroupDataSource {

    private val lock = Mutex()

    private val groupIdMap = mutableMapOf<Group.Id, Group>()


    override suspend fun add(group: Group): AddResult {
        val result = lock.withLock {
            groupIdMap.put(group.id, group)
        }

        return if(result == null) {
            AddResult.CREATED
        }else{
            AddResult.UPDATED
        }
    }

    override suspend fun addAll(groups: List<Group>): List<AddResult> {
        return groups.map {
            add(it)
        }
    }

    override suspend fun delete(groupId: Group.Id): Boolean {
        val result = lock.withLock {
            groupIdMap.remove(groupId)
        }
        return result != null
    }

    override suspend fun find(groupId: Group.Id): Group {
        return lock.withLock {
            groupIdMap[groupId]
        }?: throw GroupNotFoundException(groupId)
    }
}