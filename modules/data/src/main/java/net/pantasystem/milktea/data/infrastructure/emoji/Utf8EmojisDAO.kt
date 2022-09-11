package net.pantasystem.milktea.data.infrastructure.emoji

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface Utf8EmojisDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(emojis: List<Utf8EmojiDTO>)

    @Query("select * from utf8_emojis_by_amio")
    fun findAll(): Flow<List<Utf8EmojiDTO>>

    @Query("delete from utf8_emojis_by_amio")
    suspend fun clear()
}