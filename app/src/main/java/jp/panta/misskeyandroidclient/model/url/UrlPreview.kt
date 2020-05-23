package jp.panta.misskeyandroidclient.model.url

import com.google.gson.annotations.SerializedName

data class UrlPreview(
    val url: String,
    val title: String,
    val icon: String,
    val description: String,
    val thumbnail: String,
    @SerializedName("sitename")val siteName: String
    //val sensitive: Boolean
    //val player,
)

