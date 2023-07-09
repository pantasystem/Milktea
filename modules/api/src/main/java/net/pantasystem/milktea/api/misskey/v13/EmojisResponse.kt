package net.pantasystem.milktea.api.misskey.v13

import kotlinx.serialization.SerialName
import net.pantasystem.milktea.api.misskey.emoji.CustomEmojiNetworkDTO

@kotlinx.serialization.Serializable
data class EmojisResponse(
    @SerialName("emojis")
    val emojis: List<CustomEmojiNetworkDTO>
)