package net.pantasystem.milktea.api.misskey.favorite

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.pantasystem.milktea.api.misskey.notes.NoteDTO

@Serializable
data class Favorite(
    @SerialName("id")
    val id: String,

    @SerialName("createdAt")
    val createdAt: String,

    @SerialName("note")
    val note: NoteDTO,

    @SerialName("noteId")
    val noteId: String,
)