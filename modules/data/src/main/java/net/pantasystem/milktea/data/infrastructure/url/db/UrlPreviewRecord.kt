package net.pantasystem.milktea.data.infrastructure.url.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import net.pantasystem.milktea.model.url.UrlPreview
import java.util.Date

@Entity(tableName = "url_preview")
data class UrlPreviewRecord(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "url")
    val url: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "icon")
    val icon: String?,

    @ColumnInfo(name = "description")
    val description: String?,

    @ColumnInfo(name = "thumbnail")
    val thumbnail: String?,

    @ColumnInfo(name = "siteName")
    val siteName: String?, //val sensitive: Boolean

    @ColumnInfo(name = "createdAt")
    val createdAt: Date? = null,
) {
    companion object {
        fun from(model: UrlPreview): UrlPreviewRecord {
            return UrlPreviewRecord(
                url = model.url,
                title = model.title,
                icon = model.icon,
                description = model.description,
                thumbnail = model.thumbnail,
                siteName = model.siteName,

            )
        }
    }

    fun toModel(): UrlPreview {
        return UrlPreview(
            url = url,
            title = title,
            icon = icon,
            description = description,
            thumbnail = thumbnail,
            siteName = siteName
        )
    }
}
