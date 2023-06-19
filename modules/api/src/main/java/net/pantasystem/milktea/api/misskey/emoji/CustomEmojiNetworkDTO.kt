package net.pantasystem.milktea.api.misskey.emoji

import kotlinx.serialization.SerialName
import net.pantasystem.milktea.model.emoji.Emoji

@kotlinx.serialization.Serializable
data class CustomEmojiNetworkDTO(
    @SerialName("id") val id: String? = null,
    @SerialName("name") val name: String,
    @SerialName("host") val host: String? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("uri") val uri: String? = null,
    @SerialName("type") val type: String? = null,
    @SerialName("category") val category: String? = null,
    @SerialName("aliases") val aliases: List<String>? = null,
    @SerialName("width") val width: Int? = null,
    @SerialName("height") val height: Int? = null,
) {
    fun toModel(aspectRatio: Float? = null, cachePath: String? = null): Emoji {
        return Emoji(
            id = id,
            name = name,
            host = host,
            url = url,
            uri = uri,
            type = type,
            category = category,
            aliases = aliases,
            aspectRatio = aspectRatio ?: if (width == null || height == null || height <= 0) null else width.toFloat() / height,
            cachePath = cachePath,
        )
    }
}