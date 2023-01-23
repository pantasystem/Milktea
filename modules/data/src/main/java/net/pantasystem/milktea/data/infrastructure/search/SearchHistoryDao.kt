package net.pantasystem.milktea.data.infrastructure.search

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: SearchHistoryRecord): Long

    @Delete
    suspend fun delete(record: SearchHistoryRecord)

    @Query("select * from search_histories where accountId = :accountId order by id desc limit :limit")
    fun observeByAccountId(accountId: Long, limit: Int): Flow<List<SearchHistoryRecord>>

    @Query("select * from search_histories where id = :id")
    suspend fun findOne(id: Long): SearchHistoryRecord?
}