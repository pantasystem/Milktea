package net.pantasystem.milktea.model.notes.reaction

import net.pantasystem.milktea.model.notes.Note

interface ReactionRepository {

    suspend fun create(createReaction: CreateReaction): Result<Boolean>
    suspend fun delete(noteId: Note.Id): Result<Boolean>

}