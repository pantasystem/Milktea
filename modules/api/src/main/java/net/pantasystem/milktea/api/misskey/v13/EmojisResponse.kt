package net.pantasystem.milktea.api.misskey.v13

import net.pantasystem.milktea.model.emoji.Emoji

@kotlinx.serialization.Serializable
data class EmojisResponse(
    val emojis: List<Emoji>
)