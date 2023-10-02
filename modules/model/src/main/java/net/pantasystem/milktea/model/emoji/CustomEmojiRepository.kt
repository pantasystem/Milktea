package net.pantasystem.milktea.model.emoji

import kotlinx.coroutines.flow.Flow

interface CustomEmojiRepository {

    suspend fun findBy(host: String): Result<List<CustomEmoji>>

    suspend fun sync(host: String): Result<Unit>

    /**
     * 完全一致で絵文字を探索する
     */
    suspend fun findByName(host: String, name: String): Result<List<CustomEmoji>>

    suspend fun addEmojis(host: String, emojis: List<EmojiWithAlias>): Result<Unit>

    suspend fun deleteEmojis(host: String, emojis: List<CustomEmoji>): Result<Unit>

    fun observeBy(host: String, withAliases: Boolean = false): Flow<List<CustomEmoji>>

    fun observeWithSearch(host: String, keyword: String): Flow<List<CustomEmoji>>

    suspend fun search(host: String, keyword: String): Result<List<CustomEmoji>>

    fun get(host: String): List<CustomEmoji>?

    fun getAndConvertToMap(host: String): Map<String, CustomEmoji>?

}

