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
    val authorName: String? = null,

    @SerialName("author_url")
    val authorUrl: String? = null,

    @SerialName("provider_url")
    val providerUrl: String,

    @SerialName("html")
    val html: String? = null,

    @SerialName("width")
    val width: Int? = null,

    @SerialName("height")
    val height: Int? = null,

    @SerialName("image")
    val image: String?,

    @SerialName("embed_url")
    val embedUrl: String? = null,

    @SerialName("blurhash")
    val blurhash: String? = null,
)