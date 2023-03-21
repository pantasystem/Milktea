package net.pantasystem.milktea.api.misskey.v13

import kotlinx.serialization.SerialName
import net.pantasystem.milktea.model.emoji.Emoji

@kotlinx.serialization.Serializable
data class EmojisResponse(
    @SerialName("emojis")
    val emojis: List<Emoji>
)