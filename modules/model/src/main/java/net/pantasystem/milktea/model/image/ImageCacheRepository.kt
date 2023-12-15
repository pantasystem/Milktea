package net.pantasystem.milktea.model.image

interface ImageCacheRepository {
    suspend fun save(url: String): Result<ImageCache>
    suspend fun findBySourceUrl(url: String): Result<ImageCache?>
    suspend fun deleteExpiredCaches(): Result<Unit>
    suspend fun clear(): Result<Unit>

    suspend fun findBySourceUrls(urls: List<String>): Result<List<ImageCache>>

    suspend fun findCachedFileCount(reality: Boolean = false): Result<Long>
}