package net.pantasystem.milktea.model.notes.reaction

import net.pantasystem.milktea.model.notes.Note

interface ReactionRepository {

    suspend fun create(createReaction: CreateReaction): Result<Unit>
    suspend fun delete(noteId: Note.Id): Result<Unit>

}