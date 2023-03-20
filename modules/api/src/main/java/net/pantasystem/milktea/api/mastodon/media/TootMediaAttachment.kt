package net.pantasystem.milktea.api.mastodon.media

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class TootMediaAttachment(
    @SerialName("id")
    val id: String,

    @SerialName("type")
    val type: String,

    @SerialName("url")
    val url: String?,

    @SerialName("preview_url")
    val previewUrl: String? = null,

    @SerialName("remote_url")
    val remoteUrl: String? = null,

    @SerialName("description")
    val description: String? = null,

    @SerialName("blurhash")
    val blurhash: String? = null,

)