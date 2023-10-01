package net.pantasystem.milktea.model.emoji

import java.io.Serializable

data class CustomEmoji(
    val id: String? = null,
    val name: String,
    val host: String? = null,
    val url: String? = null,
    val uri: String? = null,
    val type: String? = null,
    val category: String? = null,
    val aspectRatio: Float? = null,
    val cachePath: String? = null,
): Serializable {

    constructor(name: String) : this(null, name, null, null, null, null, null, null)

    fun getLoadUrl(): String? {
        return cachePath ?: url ?: uri
    }
}

data class EmojiWithAlias(
    val emoji: CustomEmoji,
    val aliases: List<String>?,
)