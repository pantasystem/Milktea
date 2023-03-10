package net.pantasystem.milktea.data.infrastructure.filter.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MastodonFilterDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(filters: List<MastodonWordFilterRecord>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(filter: MastodonWordFilterRecord): Long

    @Query("""
        select * from mastodon_word_filters_v1 where accountId = :accountId
    """)
    suspend fun findByAccount(accountId: Long): List<MastodonWordFilterRecord>

    @Query("""
        select * from mastodon_word_filters_v1 where accountId = :accountId
    """)
    fun observeByAccount(accountId: Long): Flow<List<MastodonWordFilterRecord>>

    @Query("""
       delete from mastodon_word_filters_v1 where accountId = :accountId
    """)
    suspend fun deleteByAccount(accountId: Long)
}