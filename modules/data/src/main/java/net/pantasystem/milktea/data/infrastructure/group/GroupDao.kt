package net.pantasystem.milktea.data.infrastructure.group

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {

    @Insert
    suspend fun insert(group: GroupRecord): Long

    @Update
    suspend fun update(group: GroupRecord)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUserIds(members: List<GroupMemberIdRecord>): List<Long>

    @Delete
    suspend fun deleteMember(member: GroupMemberIdRecord)

    @Delete
    suspend fun delete(group: GroupRecord)

    @Query("delete from group_v1 where accountId = :accountId and serverId = :serverId")
    suspend fun delete(accountId: Long, serverId: String)

    @Query("delete from group_v1 where accountId = :accountId")
    suspend fun clearByAccountId(accountId: Long)

    @Query("delete from group_member_v1 where groupId = :groupId")
    suspend fun detachMembers(groupId: Long)


    @Query(
        """
        select * from group_v1 where exists (
            select 1 from group_member_v1 
                where group_v1.id = group_member_v1.groupId 
                    and group_member_v1.groupId = :userId
                    and group_v1.accountId = :accountId
        )
    """
    )
    @Transaction
    fun findJoinedGroups(accountId: Long, userId: String): List<GroupRelatedRecord>

    @Query(
        """
        select * from group_v1 where exists (
            select 1 from group_member_v1 
                where group_v1.id = group_member_v1.groupId 
                    and group_member_v1.groupId = group_v1.id
                    and group_member_v1.userId = :userId
                    and group_v1.accountId = :accountId
        )
    """
    )
    @Transaction
    fun observeJoinedGroups(accountId: Long, userId: String): Flow<List<GroupRelatedRecord>?>


    @Query(
        """
        select * from group_v1 where accountId = :accountId
            and ownerId = :userId
        """
    )
    @Transaction
    suspend fun findOwnedGroups(accountId: Long, userId: String): List<GroupRelatedRecord>

    @Query(
        """
        select * from group_v1 where accountId = :accountId
            and ownerId = :userId
        """
    )
    @Transaction
    fun observeOwnedGroups(accountId: Long, userId: String): Flow<List<GroupRelatedRecord>?>


    @Query(
        """
        select * from group_v1 where accountId = :accountId
            and serverId = :serverId
        """
    )
    @Transaction
    suspend fun findOne(accountId: Long, serverId: String): GroupRelatedRecord?

    @Query(
        """
        select * from group_v1 where accountId = :accountId
            and serverId = :serverId
        """
    )
    @Transaction
    fun observeOne(accountId: Long, serverId: String): Flow<GroupRelatedRecord?>
}