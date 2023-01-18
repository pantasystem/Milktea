package net.pantasystem.milktea.api.mastodon.emojis

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.pantasystem.milktea.model.emoji.Emoji

@Serializable
data class TootEmojiDTO(
    val shortcode: String,
    val url: String,

    @SerialName("static_url")
    val staticUrl: String,
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