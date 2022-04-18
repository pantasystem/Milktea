package net.pantasystem.milktea.api.mastodon.emojis

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.pantasystem.milktea.model.emoji.Emoji

@Serializable
data class TootEmojiDTO (
    val shortcode: String,
    val url: String,
    @SerializedName("static_url")
    @SerialName("static_url")
    val staticUrl: String,
    val category: String? = null,
    @SerializedName("visible_in_picker")
    @SerialName("visible_in_picker")
    val visibleInPicker: Boolean = true
) {

    fun toEmoji(host: String): net.pantasystem.milktea.model.emoji.Emoji {
        return net.pantasystem.milktea.model.emoji.Emoji(
            name = shortcode,
            url = url,
            category = category,
        )
    }
}