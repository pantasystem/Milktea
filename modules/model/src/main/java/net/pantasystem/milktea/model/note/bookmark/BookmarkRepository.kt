package net.pantasystem.milktea.model.note.bookmark

import net.pantasystem.milktea.model.note.Note

interface BookmarkRepository {

    suspend fun create(noteId: Note.Id): Result<Unit>

    suspend fun delete(noteId: Note.Id): Result<Unit>

}