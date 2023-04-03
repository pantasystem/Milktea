package net.pantasystem.milktea.model.emoji

import kotlinx.coroutines.flow.Flow

interface CustomEmojiRepository {

    suspend fun findBy(host: String): Result<List<Emoji>>

    suspend fun sync(host: String): Result<Unit>

    /**
     * 完全一致で絵文字を探索する
     */
    suspend fun findByName(host: String, name: String): Result<List<Emoji>>

    suspend fun addEmojis(host: String, emojis: List<Emoji>): Result<Unit>

    suspend fun deleteEmojis(host: String, emojis: List<Emoji>): Result<Unit>

    fun observeBy(host: String): Flow<List<Emoji>>

    fun get(host: String): List<Emoji>?

    fun getAndConvertToMap(host: String): Map<String, Emoji>?

}

