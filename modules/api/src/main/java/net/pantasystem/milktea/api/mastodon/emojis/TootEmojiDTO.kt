package net.pantasystem.milktea.api.mastodon.emojis

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.pantasystem.milktea.model.emoji.CustomEmoji
import net.pantasystem.milktea.model.emoji.EmojiWithAlias

@Serializable
data class TootEmojiDTO(
    @SerialName("shortcode")
    val shortcode: String,

    @SerialName("url")
    val url: String,

    @SerialName("static_url")
    val staticUrl: String,

    @SerialName("category")
    val category: String? = null,

    @SerialName("visible_in_picker")
    val visibleInPicker: Boolean = true,

    @SerialName("width")
    val width: Int? = null,

    @SerialName("height")
    val height: Int? = null,

    @SerialName("aliases")
    val aliases: List<String?>? = null,
) {

    fun toEmojiWithAlias(cachePath: String? = null): EmojiWithAlias {
        val emoji = toEmoji(cachePath)
        return EmojiWithAlias(
            emoji = emoji,
            aliases = aliases?.filterNotNull(),
        )
    }

    fun toEmoji(cachePath: String? = null): CustomEmoji {
        return CustomEmoji(
            name = shortcode,
            url = url,
            category = category,
            aspectRatio = if (width == null || height == null) null else (width.toFloat() / height),
            cachePath = cachePath,
        )
    }
}