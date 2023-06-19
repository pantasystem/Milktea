package net.pantasystem.milktea.data.infrastructure.image

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Unique
import kotlinx.datetime.Instant
import net.pantasystem.milktea.model.image.ImageCache

@Entity
data class ImageCacheRecord(
    @Id var id: Long = 0L,
    @Unique var sourceUrl: String = "",
    var cachePath: String = "",
    var cachedAt: Long = 0L,
) {

    companion object {
        fun from(model: ImageCache): ImageCacheRecord {
            return ImageCacheRecord().also {
                it.applyModel(model)
            }
        }
    }
    fun applyModel(model: ImageCache) {
        sourceUrl = model.sourceUrl
        cachePath = model.cachePath
        cachedAt = model.cachedAt.toEpochMilliseconds()
    }

    fun toModel(): ImageCache {
        return ImageCache(
            sourceUrl = sourceUrl,
            cachePath = cachePath,
            cachedAt = Instant.fromEpochMilliseconds(cachedAt)
        )
    }

}