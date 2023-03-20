package net.pantasystem.milktea.api.misskey.clip

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class FindNotesClip(
    @SerialName("i")
    val i: String,

    @SerialName("noteId")
    val noteId: String
)