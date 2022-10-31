package net.pantasystem.milktea.data.infrastructure.list

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserListDao {

    @Query(
        """select * from user_list where accountId = :accountId"""
    )
    @Transaction
    fun observeUserListRelatedWhereByAccountId(
        accountId: Long,
    ): Flow<List<UserListRelatedRecord>>

    @Query(
        """select * from user_list where accountId = :accountId"""
    )
    @Transaction
    suspend fun findUserListRelatedWhereByAccountId(
        accountId: Long,
    ): List<UserListRelatedRecord>

    @Query(
        """
            select * from user_list where accountId = :accountId
                and serverId in (:serverIds)
        """
    )
    @Transaction
    suspend fun findUserListWhereIn(
        accountId: Long,
        serverIds: List<String>
    ): List<UserListRecord>

    @Query(
        """
            select * from user_list 
                where accountId = :accountId
                and serverId = :serverId
            """
    )
    @Transaction
    suspend fun findByServerId(
        accountId: Long,
        serverId: String
    ): UserListRelatedRecord?

    @Query(
        """
            select * from user_list 
                where accountId = :accountId
                and serverId = :serverId
            """
    )
    @Transaction
    fun observeByServerId(
        accountId: Long,
        serverId: String,
    ): Flow<UserListRelatedRecord?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(userList: UserListRecord): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(userList: List<UserListRecord>): List<Long>

    @Update
    suspend fun update(userListRecord: UserListRecord)

    @Query(
        """
            delete from user_list_member
                where userListId = :userListId
        """
    )
    suspend fun detachUserIds(userListId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun attachMemberIds(memberIds: List<UserListMemberIdRecord>)

    @Query(
        """
            delete from user_list
                where accountId = :accountId
        """
    )
    suspend fun deleteByAccountId(accountId: Long)


}