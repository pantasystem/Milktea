package net.pantasystem.milktea.api.mastodon.media

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class UpdateMediaAttachment(
    @SerialName("description")
    val description: String,

    @SerialName("focus")
    val focus: String
)