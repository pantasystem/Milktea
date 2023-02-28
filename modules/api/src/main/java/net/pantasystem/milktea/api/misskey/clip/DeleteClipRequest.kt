package net.pantasystem.milktea.api.misskey.clip

@kotlinx.serialization.Serializable
data class DeleteClipRequest(
    val i: String,
    val clipId: String,
)