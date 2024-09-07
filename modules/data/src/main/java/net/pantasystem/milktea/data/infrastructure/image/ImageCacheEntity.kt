package net.pantasystem.milktea.data.infrastructure.image

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import net.pantasystem.milktea.model.image.ImageCache

@Entity(
    tableName = "image_caches",
    indices = [
        Index(
            "source_url",
            unique = true,
        ),
        Index(
            "cached_at"
        )
    ]
)
data class ImageCacheEntity(
    @ColumnInfo("source_url") val sourceUrl: String,
    @ColumnInfo("cache_path") val cachePath: String,
    @ColumnInfo("cached_at") val cachedAt: Long,
    @ColumnInfo("width") val width: Int?,
    @ColumnInfo("height") val height: Int?,
    @ColumnInfo("id") @PrimaryKey(autoGenerate = true) val id: Long = 0L,
) {

    companion object {
        fun from(model: ImageCache): ImageCacheEntity {
            return ImageCacheEntity(
                sourceUrl = model.sourceUrl,
                cachePath = model.cachePath,
                cachedAt = model.cachedAt.toEpochMilliseconds(),
                width = model.width,
                height = model.height,
            )
        }
    }

    fun toModel(): ImageCache {
        return ImageCache(
            sourceUrl = sourceUrl,
            cachePath = cachePath,
            cachedAt = Instant.fromEpochMilliseconds(cachedAt),
            width = width,
            height = height,
        )
    }
}