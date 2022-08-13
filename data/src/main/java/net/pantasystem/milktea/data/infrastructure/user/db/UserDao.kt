package net.pantasystem.milktea.data.infrastructure.user.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
abstract class UserDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(user: UserRecord): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertUsers(users: List<UserRecord>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertPinnedNoteIds(ids: List<PinnedNoteIdRecord>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(detail: UserDetailedStateRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertDetails(users: List<UserDetailedStateRecord>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertEmojis(emojis: List<UserEmojiRecord>): List<Long>


    @Query("delete from pinned_note_id where userId = :userId")
    abstract suspend fun detachAllPinnedNoteIds(userId: Long)

    @Query("delete from user_emoji where userId = :userId")
    abstract suspend fun detachAllUserEmojis(userId: Long)

    @Update
    abstract suspend fun update(user: UserRecord)

    @Update
    abstract suspend fun update(user: UserDetailedStateRecord)

    @Query(
        """
            select * from user_view where accountId = :accountId and serverId in (:serverIds)
        """
    )
    @Transaction
    abstract fun observeInServerIds(accountId: Long, serverIds: List<String>): Flow<List<UserRelated>>

    @Query(
        """
            select * from user_view where id in (:ids)
        """
    )
    @Transaction
    abstract fun observeInIds(ids: List<Long>): Flow<List<UserRelated>>

    @Query("""
        select * from user_view where id = :id
    """)
    @Transaction
    abstract suspend fun get(id: Long): UserRelated?

    @Query("""
        select * from user_view where accountId = :accountId and serverId = :serverId
    """)
    @Transaction
    abstract suspend fun get(accountId: Long, serverId: String): UserRelated?

    @Query("""
        select * from user_view where accountId = :accountId and serverId = :serverId
    """)
    @Transaction
    abstract fun observe(accountId: Long, serverId: String): Flow<UserRelated?>

    @Query("""
        select * from user_view where id = :id
    """)
    @Transaction
    abstract fun observe(id: Long): Flow<UserRelated?>

    @Query("""
        select * from user_view where accountId = :accountId and userName = :userName and host = :host
    """)
    @Transaction
    abstract suspend fun getByUserName(accountId: Long, userName: String, host: String): UserRelated?

    @Query("""
        select * from user_view where accountId = :accountId and userName = :userName
    """)
    @Transaction
    abstract suspend fun getByUserName(accountId: Long, userName: String): UserRelated?

    @Query("""
        select * from user_view where accountId = :accountId and userName = :userName and host = :host
    """)
    @Transaction
    abstract fun observeByUserName(accountId: Long, userName: String, host: String): Flow<UserRelated?>

    @Query("""
        select * from user_view where accountId = :accountId and userName = :userName
    """)
    @Transaction
    abstract fun observeByUserName(accountId: Long, userName: String): Flow<UserRelated?>

    @Query("""
        select * from user_view where userName = :userName
    """)
    @Transaction
    abstract fun observeByUserName(userName: String): Flow<UserRelated?>

    @Query("""
        select * from user_view where userName = :userName and host = :host
    """)
    @Transaction
    abstract fun observeByUserName(userName: String, host: String): Flow<UserRelated?>

    @Query("""
        select * from user_view where accountId = :accountId and serverId in (:serverIds)
    """)
    @Transaction
    abstract suspend fun getInServerIds(accountId: Long, serverIds: List<String>): List<UserRelated>

    @Query("""
        delete from user where accountId = :accountId and serverId = :serverId
    """)
    abstract suspend fun delete(accountId: Long, serverId: String)

    @Query("""
        select * from user_view where accountId = :accountId and (name like :word or userName like :word) 
    """)
    @Transaction
    abstract suspend fun searchByName(accountId: Long, word: String): List<UserRelated>
}