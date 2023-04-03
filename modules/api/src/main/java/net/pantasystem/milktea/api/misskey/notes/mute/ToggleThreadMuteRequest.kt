package net.pantasystem.milktea.api.misskey.notes.mute

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class ToggleThreadMuteRequest(
    @SerialName("i")
    val i: String,

    @SerialName("noteId")
    val noteId: String,
)