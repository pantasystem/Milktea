package net.pantasystem.milktea.data.infrastructure.group

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.model.AddResult
import net.pantasystem.milktea.model.group.Group
import net.pantasystem.milktea.model.group.GroupDataSource
import net.pantasystem.milktea.model.group.GroupNotFoundException
import javax.inject.Inject

class GroupDataSourceImpl @Inject constructor(
    private val groupDao: GroupDao
) : GroupDataSource {

    override suspend fun add(group: Group): AddResult {
        return withContext(Dispatchers.IO) {
            val record = groupDao.findOne(group.id.accountId, group.id.groupId)
            val newEntity = GroupRecord.from(group)
            if (record == null) {
                val id = groupDao.insert(newEntity)
                groupDao.insertUserIds(
                    group.userIds.map {
                        GroupMemberIdRecord(id, it.id, 0L)
                    }
                )
                AddResult.CREATED
            } else {
                groupDao.update(newEntity.copy(id = record.group.id))
                groupDao.detachMembers(record.group.id)
                groupDao.insertUserIds(
                    group.userIds.map {
                        GroupMemberIdRecord(record.group.id, it.id, 0L)
                    }
                )
                AddResult.UPDATED
            }
        }
    }

    override suspend fun addAll(groups: List<Group>): List<AddResult> {
        return groups.map {
            add(it)
        }
    }

    override suspend fun delete(groupId: Group.Id): Boolean {
        return withContext(Dispatchers.IO) {
            val exists = groupDao.findOne(groupId.accountId, groupId.groupId) != null
            groupDao.delete(groupId.accountId, groupId.groupId)
            exists
        }
    }

    override suspend fun find(groupId: Group.Id): Group {
        return withContext(Dispatchers.IO) {
            groupDao.findOne(groupId.accountId, groupId.groupId)
                ?.toModel()
                ?: throw GroupNotFoundException(groupId)
        }
    }
}