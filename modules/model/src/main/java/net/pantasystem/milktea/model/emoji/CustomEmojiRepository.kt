package net.pantasystem.milktea.model.emoji

import kotlinx.coroutines.flow.Flow

interface CustomEmojiRepository {

    suspend fun findBy(query: HostWithName): Result<Emoji?>

    suspend fun findBy(query: HostWithCategory): Result<List<Emoji>>

    suspend fun findBy(host: String): Result<List<Emoji>>

    suspend fun sync(host: String): Result<Unit>

    fun observeBy(query: HostWithName): Flow<Emoji?>

    fun observeBy(host: String): Flow<List<Emoji>>

    fun observeBy(host: HostWithCategory): Flow<List<Emoji>>

}

data class HostWithName(
    val host: String,
    val name: String
)

data class HostWithCategory(
    val host: String,
    val category: String,
)
