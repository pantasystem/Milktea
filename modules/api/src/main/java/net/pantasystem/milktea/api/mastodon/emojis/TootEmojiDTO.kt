package net.pantasystem.milktea.api.mastodon.emojis

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.pantasystem.milktea.model.emoji.Emoji

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
    val visibleInPicker: Boolean = true
) {

    fun toEmoji(): Emoji {
        return Emoji(
            name = shortcode,
            url = url,
            category = category,
        )
    }
}