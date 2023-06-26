package net.pantasystem.milktea.model.image

interface ImageCacheRepository {
    suspend fun save(url: String): ImageCache
    suspend fun findBySourceUrl(url: String): ImageCache?
    suspend fun deleteExpiredCaches()
    suspend fun clear()

    suspend fun findBySourceUrls(urls: List<String>): List<ImageCache>

    suspend fun findCachedFileCount(reality: Boolean = false): Long
}