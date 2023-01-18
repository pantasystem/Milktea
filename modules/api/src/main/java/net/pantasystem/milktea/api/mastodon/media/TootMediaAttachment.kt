package net.pantasystem.milktea.api.mastodon.media

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class TootMediaAttachment(
    val id: String,
    val type: String,
    val url: String,
    @SerialName("preview_url") val previewUrl: String,
    @SerialName("remote_url") val remoteUrl: String,
    val description: String? = null,
    val blurhash: String? = null,

)