package net.pantasystem.milktea.api.misskey.clip

@kotlinx.serialization.Serializable
data class AddNoteToClipRequest(
    val i: String,
    val clipId: String,
    val noteId: String
)

typealias RemoteNoteToClipRequest = AddNoteToClipRequest