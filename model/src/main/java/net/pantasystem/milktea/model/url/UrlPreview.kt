package net.pantasystem.milktea.model.url

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity(tableName = "url_preview")
@Serializable
data class UrlPreview(
    @PrimaryKey(autoGenerate = false) val url: String,
    val title: String,
    val icon: String?,
    val description: String?,
    val thumbnail: String?,
    @SerialName("sitename") val siteName: String?
    //val sensitive: Boolean
    //val player,
)

