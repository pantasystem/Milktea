package net.pantasystem.milktea.api.misskey.clip

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class AddNoteToClipRequest(
    @SerialName("i")
    val i: String,

    @SerialName("clipId")
    val clipId: String,

    @SerialName("noteId")
    val noteId: String
)


typealias RemoveNoteToClipRequest = AddNoteToClipRequest