package net.pantasystem.milktea.api.misskey.clip

@kotlinx.serialization.Serializable
data class ShowClipRequest(
    val i: String,
    val clipId: String
)