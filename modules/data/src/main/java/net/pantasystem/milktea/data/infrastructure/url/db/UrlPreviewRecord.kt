package net.pantasystem.milktea.data.infrastructure.url.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.pantasystem.milktea.model.url.UrlPreview

@Entity(tableName = "url_preview")
@Serializable
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

    @SerialName("sitename")
    val siteName: String?
    //val sensitive: Boolean
    //val player,
) {
    companion object {
        fun from(model: UrlPreview): UrlPreviewRecord {
            return UrlPreviewRecord(
                url = model.url,
                title = model.title,
                icon = model.icon,
                description = model.description,
                thumbnail = model.thumbnail,
                siteName = model.siteName
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
