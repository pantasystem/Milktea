package net.pantasystem.milktea.data.model.fevorite

import net.pantasystem.milktea.api.misskey.notes.NoteDTO


data class Favorite(val id: String, val createdAt: String, val note: NoteDTO, val noteId: String)