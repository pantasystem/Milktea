package net.pantasystem.milktea.model.note.favorite

import net.pantasystem.milktea.model.note.Note

interface FavoriteRepository {

    suspend fun create(noteId: Note.Id): Result<Unit>
    suspend fun delete(noteId: Note.Id): Result<Unit>
}