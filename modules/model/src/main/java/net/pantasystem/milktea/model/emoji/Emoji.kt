package net.pantasystem.milktea.model.emoji

import kotlinx.serialization.SerialName
import java.io.Serializable

@kotlinx.serialization.Serializable
data class Emoji(
    @SerialName("id") val id: String? = null,
    @SerialName("name") val name: String,
    @SerialName("host") val host: String? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("uri") val uri: String? = null,
    @SerialName("type") val type: String? = null,
    @SerialName("category") val category: String? = null,
    @kotlinx.serialization.Transient val aspectRatio: Float? = null,
    @kotlinx.serialization.Transient val cachePath: String? = null,
): Serializable{

    constructor(name: String) : this(null, name, null, null, null, null, null, null)

    fun getLoadUrl(): String? {
        return cachePath ?: url ?: uri
    }
}

data class EmojiWithAlias(
    val emoji: Emoji,
    val aliases: List<String>?,
)