package net.pantasystem.milktea.data.infrastructure.emoji

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface CustomEmojiAspectRatioDAO {

    @Query("""
        SELECT * FROM custom_emoji_aspects WHERE uri in (:uris)
    """)
    suspend fun findIn(uris: List<String>): List<CustomEmojiAspectRatioEntity>

    @Query("""
        SELECT * FROM custom_emoji_aspects WHERE uri = :uri
    """)
    suspend fun findOne(uri: String): CustomEmojiAspectRatioEntity?

    @Upsert
    suspend fun upsert(entity: CustomEmojiAspectRatioEntity)

    @Query("DELETE FROM custom_emoji_aspects WHERE uri = :uri")
    suspend fun delete(uri: String)
}