package net.pantasystem.milktea.data.infrastructure.note.wordmute

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WordFilterConfigDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(conditions: List<WordFilterConditionRecord>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegex(regex: WordFilterConditionRegexRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<WordFilterConditionWordRecord>): List<Long>

    @Query("select * from word_filter_condition order by id asc")
    @Transaction
    suspend fun findAll(): List<WordFilterConditionRelated>

    @Query("select * from word_filter_condition order by id asc")
    @Transaction
    fun observeAll(): Flow<List<WordFilterConditionRelated>>

    @Query("delete from word_filter_condition")
    suspend fun clear()
}