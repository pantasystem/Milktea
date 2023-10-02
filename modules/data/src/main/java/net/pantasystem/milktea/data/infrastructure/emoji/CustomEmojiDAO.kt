package net.pantasystem.milktea.data.infrastructure.emoji

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomEmojiDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(emojis: List<CustomEmojiRecord>): List<Long>

    @Insert
    suspend fun insert(emoji: CustomEmojiRecord): Long

    @Query("SELECT * FROM custom_emojis WHERE emojiHost = :host")
    suspend fun findByHost(host: String): List<CustomEmojiRecord>

    @Query("SELECT * FROM custom_emojis WHERE emojiHost = :host AND name = :name")
    suspend fun findByHostAndName(host: String, name: String): List<CustomEmojiRecord>

    @Query("DELETE FROM custom_emojis WHERE emojiHost = :host")
    suspend fun deleteByHost(host: String)

    @Query("""
        DELETE FROM custom_emojis WHERE emojiHost = :host AND name IN (:names)
    """)
    suspend fun deleteByHostAndNames(host: String, names: List<String>)


    @Insert(
        onConflict = OnConflictStrategy.REPLACE,
    )
    suspend fun insertAliases(aliases: List<CustomEmojiAliasRecord>): List<Long>

    @Query("SELECT * FROM custom_emojis WHERE emojiHost = :host")
    fun observeBy(host: String): Flow<List<CustomEmojiRecord>>

    @Query("""
        SELECT * FROM custom_emojis 
            WHERE emojiHost = :host AND name LIKE '%' || :keyword || '%'
            OR EXISTS(
                SELECT 1 FROM custom_emoji_aliases WHERE emojiHost = :host AND name LIKE '%' || :keyword || '%'
                    AND custom_emoji_aliases.emojiId = custom_emojis.id
            )
    """)
    fun observeAndSearch(host: String, keyword: String): Flow<List<CustomEmojiRecord>>

    @Query("""
        SELECT * FROM custom_emojis 
            WHERE emojiHost = :host AND name LIKE '%' || :keyword || '%'
            OR EXISTS(
                SELECT 1 FROM custom_emoji_aliases WHERE emojiHost = :host AND name LIKE '%' || :keyword || '%'
                    AND custom_emoji_aliases.emojiId = custom_emojis.id
            )
    """)
    suspend fun search(host: String, keyword: String): List<CustomEmojiRecord>


}