package net.pantasystem.milktea.model.emoji

import kotlinx.coroutines.flow.Flow

interface CustomEmojiRepository {

    suspend fun findBy(host: String): Result<List<Emoji>>

    suspend fun sync(host: String): Result<Unit>

    fun observeBy(host: String): Flow<List<Emoji>>


}

