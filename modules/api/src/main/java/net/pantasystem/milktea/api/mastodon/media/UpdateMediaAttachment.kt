package net.pantasystem.milktea.api.mastodon.media

@kotlinx.serialization.Serializable
data class UpdateMediaAttachment(
    val description: String,
    val focus: String
)