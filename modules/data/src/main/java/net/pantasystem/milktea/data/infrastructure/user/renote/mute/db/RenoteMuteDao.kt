package net.pantasystem.milktea.data.infrastructure.user.renote.mute.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RenoteMuteDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(renoteMuteRecord: RenoteMuteRecord): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(records: List<RenoteMuteRecord>): List<Long>

    @Update
    suspend fun update(renoteMuteRecord: RenoteMuteRecord)

    @Query("""
        select * from renote_mute_users where accountId = :accountId
    """)
    suspend fun findByAccount(accountId: Long): List<RenoteMuteRecord>

    @Query("""
        select * from renote_mute_users where accountId = :accountId
            and userId = :userId
    """)
    suspend fun findByUser(accountId: Long, userId: String): RenoteMuteRecord?

    @Query("""
        delete from renote_mute_users where accountId = :accountId
            and userId = :userId
    """)
    suspend fun delete(accountId: Long, userId: String)

    @Query("""
        select * from renote_mute_users where accountId = :accountId
    """)
    fun observeBy(accountId: Long): Flow<List<RenoteMuteRecord>>

}