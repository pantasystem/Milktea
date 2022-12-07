package net.pantasystem.milktea.model.url

import kotlinx.serialization.Serializable


@Serializable
data class UrlPreview(
    val url: String,
    val title: String,
    val icon: String?,
    val description: String?,
    val thumbnail: String?,
    val siteName: String?
    //val sensitive: Boolean
    //val player,
)

