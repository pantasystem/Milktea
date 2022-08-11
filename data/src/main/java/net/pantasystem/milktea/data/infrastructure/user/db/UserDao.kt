package net.pantasystem.milktea.data.infrastructure.user.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
abstract class UserDao {

    @Insert
    abstract suspend fun insert(user: UserRecord): Long

    @Insert
    abstract suspend fun insertUsers(users: List<UserRecord>): List<Long>

    @Insert
    abstract suspend fun insert(detail: UserDetailedStateRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertDetails(users: List<UserDetailedStateRecord>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertEmojis(emojis: List<UserEmojiRecord>): List<Long>



    @Update
    abstract suspend fun update(user: UserRecord)

    @Update
    abstract suspend fun update(user: UserDetailedStateRecord)

    @Query(
        """
            select * from user where accountId = :accountId and serverId in (:serverIds)
        """
    )
    @Transaction
    abstract fun observeInServerIds(accountId: Long, serverIds: List<String>): Flow<List<UserRelated>>

    @Query(
        """
            select * from user where id in (:ids)
        """
    )
    @Transaction
    abstract fun observeInIds(ids: List<Long>): Flow<List<UserRelated>>

    @Query("""
        select * from user where id = :id
    """)
    abstract suspend fun get(id: Long): UserRelated?

    @Query("""
        select * from user where accountId = :accountId and serverId = :serverId
    """)
    abstract suspend fun get(accountId: Long, serverId: String): UserRelated?

    @Query("""
        select * from user where id = :id
    """)
    abstract fun observe(id: Long): UserRelated?

    @Query("""
        select * from user where accountId = :accountId and userName = :userName and host = :host
    """)
    abstract suspend fun getByUserName(accountId: Long, userName: String, host: String): UserRelated?

    @Query("""
        select * from user where accountId = :accountId and userName = :userName
    """)
    abstract suspend fun getByUserName(accountId: Long, userName: String): UserRelated?

    @Query("""
        select * from user where accountId = :accountId and serverId in (:serverIds)
    """)
    abstract suspend fun getInServerIds(accountId: Long, serverIds: List<String>): List<UserRelated>
}