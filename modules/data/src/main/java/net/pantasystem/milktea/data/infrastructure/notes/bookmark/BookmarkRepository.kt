package net.pantasystem.milktea.data.infrastructure.notes.bookmark

import net.pantasystem.milktea.model.notes.Note

interface BookmarkRepository {

    suspend fun create(noteId: Note.Id): Result<Unit>

    suspend fun delete(noteId: Note.Id): Result<Unit>

}