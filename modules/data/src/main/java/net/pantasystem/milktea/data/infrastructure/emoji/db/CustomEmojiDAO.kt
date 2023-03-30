package net.pantasystem.milktea.data.infrastructure.emoji.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomEmojiDAO {

    @Transaction
    @Query("select * from custom_emojis where emojiHost = :host and name = :name")
    suspend fun findBy(host: String, name: String): List<CustomEmojiRelated>


    @Transaction
    @Query("select * from custom_emojis where emojiHost = :host")
    suspend fun findBy(host: String): List<CustomEmojiRelated>

    @Transaction
    @Query("select * from custom_emojis where emojiHost = :host")
    fun observeBy(host: String): Flow<List<CustomEmojiRelated>>


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(customEmoji: CustomEmojiRecord): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(emojis: List<CustomEmojiRecord>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAliases(emojis: List<CustomEmojiAliasRecord>)

    @Query("delete from custom_emoji_aliases where emojiId = :emojiId")
    suspend fun deleteAliasByEmojiId(emojiId: Long)

    @Query("delete from custom_emojis where emojiHost = :host")
    suspend fun deleteByHost(host: String)

    @Query("delete from custom_emojis where emojiHost = :host and name in (:names)")
    suspend fun deleteByHostAndNames(host: String, names: List<String>)

    @Update
    suspend fun update(customEmoji: CustomEmojiRecord)
}