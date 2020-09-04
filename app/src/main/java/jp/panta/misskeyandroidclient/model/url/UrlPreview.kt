package jp.panta.misskeyandroidclient.model.url

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "url_preview")
data class UrlPreview(
    @PrimaryKey(autoGenerate = false) val url: String,
    val title: String,
    val icon: String,
    val description: String,
    val thumbnail: String,
    @SerializedName("sitename")val siteName: String
    //val sensitive: Boolean
    //val player,
)

