package net.pantasystem.milktea.model.image

interface ImageCacheRepository {
    suspend fun save(url: String): ImageCache
    suspend fun findBySourceUrl(url: String): ImageCache?
    suspend fun deleteExpiredCaches()
    suspend fun clear()
}