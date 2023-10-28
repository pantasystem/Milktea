package net.pantasystem.milktea.data.infrastructure.user.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
abstract class UserDao {

    @Insert
    abstract suspend fun insert(user: UserRecord): Long



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertPinnedNoteIds(ids: List<PinnedNoteIdRecord>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(info: UserInfoStateRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(related: UserRelatedStateRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(users: List<UserRecord>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertEmojis(emojis: List<UserEmojiRecord>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertUserInstanceInfo(info: UserInstanceInfoRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertUserProfileFields(fields: List<UserProfileFieldRecord>): List<Long>

    @Query("delete from pinned_note_id where userId = :userId")
    abstract suspend fun detachAllPinnedNoteIds(userId: Long)

    @Query("delete from user_emoji where userId = :userId")
    abstract suspend fun detachAllUserEmojis(userId: Long)

    @Query("delete from user_profile_field where userId = :userId")
    abstract suspend fun detachUserFields(userId: Long)

    @Update
    abstract suspend fun update(user: UserRecord)


    @Update
    abstract suspend fun update(info: UserInfoStateRecord)

    @Update
    abstract suspend fun update(related: UserRelatedStateRecord)

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
    abstract suspend fun getSimple(accountId: Long, serverId: String): UserSimpleRelated?

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
        select * from user_view where accountId = :accountId and serverId in (:serverIds)
    """)
    @Transaction
    abstract suspend fun getInServerIds(accountId: Long, serverIds: List<String>): List<UserRelated>

    @Query("""
        select * from user_view where accountId = :accountId and serverId in (:serverIds)
    """)
    @Transaction
    abstract suspend fun getSimplesInServerIds(accountId: Long, serverIds: List<String>): List<UserSimpleRelated>

    @Query("""
        delete from user where accountId = :accountId and serverId = :serverId
    """)
    abstract suspend fun delete(accountId: Long, serverId: String)


    @Query("""
       select * from user_view
            where accountId = :accountId
            and serverId >= :nextId
            and (name like :word or userName like :word)
            order by serverId asc
            limit :limit
    """)
    @Transaction
    abstract suspend fun searchByNameOrUserName(accountId: Long, word: String, limit: Int, nextId: String): List<UserRelated>

    @Query("""
       select * from user_view
            where accountId = :accountId
            and (name like :word or userName like :word)
            order by serverId asc
            limit :limit
    """)
    @Transaction
    abstract suspend fun searchByNameOrUserName(accountId: Long, word: String, limit: Int): List<UserRelated>

    @Query("""
       select * from user_view
            where accountId = :accountId
            and host like :host
            and serverId >= :nextId
            and (name like :word or userName like :word)
            order by serverId asc
            limit :limit
    """)
    @Transaction
    abstract suspend fun searchByNameOrUserNameWithHost(accountId: Long, word: String, limit: Int, nextId: String, host: String): List<UserRelated>

    @Query("""
       select * from user_view
            where accountId = :accountId
            and host like :host
            and (name like :word or userName like :word)
            order by serverId asc
            limit :limit
    """)
    @Transaction
    abstract suspend fun searchByNameOrUserNameWithHost(accountId: Long, word: String, limit: Int, host: String): List<UserRelated>
}