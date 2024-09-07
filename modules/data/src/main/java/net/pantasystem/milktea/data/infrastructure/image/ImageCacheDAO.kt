package net.pantasystem.milktea.data.infrastructure.image

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface ImageCacheDAO {

    @Query(
        """
            SELECT * FROM image_caches WHERE source_url = :url AND cached_at > :newerThan
        """
    )
    suspend fun findBySourceUrl(url: String, newerThan: Long): ImageCacheEntity?

    @Query(
        """
            SELECT * FROM image_caches WHERE cached_at < :olderThan
        """
    )
    suspend fun findOlder(olderThan: Long): List<ImageCacheEntity>

    @Query(
        """
            DELETE FROM image_caches;
        """
    )
    suspend fun clear()

    @Query(
        """
            SELECT * FROM image_caches WHERE source_url in (:urls)
                AND cached_at > :newerThan
        """
    )
    suspend fun findBySourceUrls(urls: List<String>, newerThan: Long): List<ImageCacheEntity>

    @Upsert
    suspend fun upsert(entity: ImageCacheEntity): Long


    @Query(
        """
            SELECT count(*) FROM image_caches
        """
    )
    suspend fun count(): Long

    @Query("""DELETE FROM image_caches WHERE source_url = :url""")
    suspend fun deleteByUrl(url: String)

    @Query("""SELECT * FROM image_caches""")
    suspend fun findAll(): List<ImageCacheEntity>
}