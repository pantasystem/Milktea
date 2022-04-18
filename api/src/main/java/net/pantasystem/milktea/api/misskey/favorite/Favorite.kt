package net.pantasystem.milktea.api.misskey.favorite

import net.pantasystem.milktea.api.misskey.notes.NoteDTO


data class Favorite(val id: String, val createdAt: String, val note: NoteDTO, val noteId: String)