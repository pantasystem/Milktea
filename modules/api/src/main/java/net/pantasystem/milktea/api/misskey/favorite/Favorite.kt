package net.pantasystem.milktea.api.misskey.favorite

import kotlinx.serialization.Serializable
import net.pantasystem.milktea.api.misskey.notes.NoteDTO

@Serializable
data class Favorite(val id: String, val createdAt: String, val note: NoteDTO, val noteId: String)