package net.pantasystem.milktea.api.misskey.notes.mute

@kotlinx.serialization.Serializable
data class ToggleThreadMuteRequest(
    val i: String,
    val noteId: String,
)