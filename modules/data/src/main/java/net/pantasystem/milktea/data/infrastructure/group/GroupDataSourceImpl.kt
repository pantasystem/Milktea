package net.pantasystem.milktea.data.infrastructure.group

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.model.AddResult
import net.pantasystem.milktea.model.account.GetAccount
import net.pantasystem.milktea.model.group.*
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject

class GroupDataSourceImpl @Inject constructor(
    private val groupDao: GroupDao,
    private val getAccount: GetAccount,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : GroupDataSource {

    override suspend fun add(group: Group): Result<AddResult> = runCancellableCatching {
        withContext(ioDispatcher) {
            val record = groupDao.findOne(group.id.accountId, group.id.groupId)
            val newEntity = GroupRecord.from(group)
            if (record == null) {
                val id = groupDao.insert(newEntity)
                groupDao.insertUserIds(
                    group.userIds.map {
                        GroupMemberIdRecord(id, it.id, 0L)
                    }
                )
                AddResult.Created
            } else {
                groupDao.update(newEntity.copy(id = record.group.id))
                groupDao.detachMembers(record.group.id)
                groupDao.insertUserIds(
                    group.userIds.map {
                        GroupMemberIdRecord(record.group.id, it.id, 0L)
                    }
                )
                AddResult.Updated
            }
        }
    }

    override suspend fun addAll(groups: List<Group>): Result<List<AddResult>> = runCancellableCatching {
        groups.map {
            add(it).getOrElse {
                AddResult.Canceled
            }
        }
    }

    override suspend fun delete(groupId: Group.Id): Result<Boolean> = runCancellableCatching {
        withContext(ioDispatcher) {
            val exists = groupDao.findOne(groupId.accountId, groupId.groupId) != null
            groupDao.delete(groupId.accountId, groupId.groupId)
            exists
        }
    }

    override suspend fun find(groupId: Group.Id): Result<Group> = runCancellableCatching {
        withContext(ioDispatcher) {
            groupDao.findOne(groupId.accountId, groupId.groupId)
                ?.toModel()
                ?: throw GroupNotFoundException(groupId)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeJoinedGroups(accountId: Long): Flow<List<GroupWithMember>> {
        return flow {
            emit(getAccount.get(accountId))
        }.flatMapLatest {
            groupDao.observeJoinedGroups(accountId, it.remoteId).distinctUntilChanged()
        }.filterNotNull().map {
            it.map { record ->
                GroupWithMember(
                    record.toModel(),
                    record.members.map {
                        GroupMember(User.Id(record.group.accountId, it.serverId), it.avatarUrl)
                    }
                )
            }
        }.distinctUntilChanged().flowOn(ioDispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeOwnedGroups(accountId: Long): Flow<List<GroupWithMember>> {
        return flow {
            emit(getAccount.get(accountId))
        }.flatMapLatest {
            groupDao.observeOwnedGroups(accountId, it.remoteId).distinctUntilChanged()
        }.filterNotNull().map { groups ->
            groups.map { record ->
                GroupWithMember(
                    record.toModel(),
                    record.members.map {
                        GroupMember(User.Id(record.group.accountId, it.serverId), it.avatarUrl)
                    }
                )
            }
        }.distinctUntilChanged().flowOn(ioDispatcher)
    }

    override fun observeOne(groupId: Group.Id): Flow<GroupWithMember> {
        return groupDao.observeOne(groupId.accountId, groupId.groupId).distinctUntilChanged().filterNotNull().map { record ->
            GroupWithMember(
                record.toModel(),
                record.members.map {
                    GroupMember(User.Id(record.group.accountId, it.serverId), it.avatarUrl)
                }
            )
        }.filterNotNull().flowOn(ioDispatcher)
    }
}