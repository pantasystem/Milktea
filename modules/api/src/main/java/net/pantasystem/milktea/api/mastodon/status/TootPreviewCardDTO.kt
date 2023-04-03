package net.pantasystem.milktea.api.mastodon.status

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class TootPreviewCardDTO(
    @SerialName("url")
    val url: String,

    @SerialName("title")
    val title: String,

    @SerialName("type")
    val type: String,

    @SerialName("description")
    val description: String,

    @SerialName("author_name")
    val authorName: String,

    @SerialName("author_url")
    val authorUrl: String,

    @SerialName("provider_url")
    val providerUrl: String,

    @SerialName("html")
    val html: String,

    @SerialName("width")
    val width: Int,

    @SerialName("height")
    val height: Int,

    @SerialName("image")
    val image: String?,

    @SerialName("embed_url")
    val embedUrl: String?,

    @SerialName("blurhash")
    val blurhash: String?
)