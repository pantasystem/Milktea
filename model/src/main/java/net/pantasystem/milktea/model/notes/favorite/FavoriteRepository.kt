package net.pantasystem.milktea.model.notes.favorite

import net.pantasystem.milktea.model.notes.Note

interface FavoriteRepository {

    suspend fun create(noteId: Note.Id): Result<Unit>
    suspend fun delete(noteId: Note.Id): Result<Unit>
}