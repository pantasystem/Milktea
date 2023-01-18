package net.pantasystem.milktea.api.mastodon.status

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class TootPreviewCardDTO(
    val url: String,
    val title: String,
    val type: String,
    val description: String,
    @SerialName("author_name") val authorName: String,
    @SerialName("author_url") val authorUrl: String,
    @SerialName("provider_url") val providerUrl: String,
    val html: String,
    val width: Int,
    val height: Int,
    val image: String?,
    @SerialName("embed_url") val embedUrl: String?,
    val blurhash: String?
)