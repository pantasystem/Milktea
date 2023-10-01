package net.pantasystem.milktea.api.misskey.emoji

import kotlinx.serialization.SerialName
import net.pantasystem.milktea.model.emoji.CustomEmoji
import net.pantasystem.milktea.model.emoji.EmojiWithAlias

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
    fun toModelWithAlias(aspectRatio: Float? = null, cachePath: String? = null): EmojiWithAlias {
        val emoji = CustomEmoji(
            id = id,
            name = name,
            host = host,
            url = url,
            uri = uri,
            type = type,
            category = category,
            aspectRatio = aspectRatio ?: if (width == null || height == null || height <= 0) null else width.toFloat() / height,
            cachePath = cachePath,
        )
        return EmojiWithAlias(
            emoji = emoji,
            aliases = aliases,
        )
    }

    fun toModel(aspectRatio: Float? = null, cachePath: String? = null): CustomEmoji {
        return CustomEmoji(
            id = id,
            name = name,
            host = host,
            url = url,
            uri = uri,
            type = type,
            category = category,
            aspectRatio = aspectRatio ?: if (width == null || height == null || height <= 0) null else width.toFloat() / height,
            cachePath = cachePath,
        )
    }
}