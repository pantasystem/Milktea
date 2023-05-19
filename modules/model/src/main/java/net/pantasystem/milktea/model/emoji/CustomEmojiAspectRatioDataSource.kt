package net.pantasystem.milktea.model.emoji

interface CustomEmojiAspectRatioDataSource {
    suspend fun save(ratio: CustomEmojiAspectRatio): Result<Unit>

    suspend fun findIn(uris: List<String>): Result<List<CustomEmojiAspectRatio>>

    suspend fun findOne(uri: String): Result<CustomEmojiAspectRatio>

    suspend fun delete(ratio: CustomEmojiAspectRatio): Result<Unit>
}


data class CustomEmojiAspectRatio(
    val uri: String,
    val aspectRatio: Float
)