package net.pantasystem.milktea.api.misskey.clip

@kotlinx.serialization.Serializable
data class FindNotesClip(
    val i: String,
    val noteId: String
)