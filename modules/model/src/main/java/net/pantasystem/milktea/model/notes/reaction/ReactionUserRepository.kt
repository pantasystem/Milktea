package net.pantasystem.milktea.model.notes.reaction

import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.user.User

interface ReactionUserRepository {
    suspend fun syncBy(noteId: Note.Id, reaction: String): Result<Unit>
    suspend fun observeBy(noteId: Note.Id, reaction: String): Flow<List<User>>
    suspend fun findBy(noteId: Note.Id, reaction: String): Result<List<User>>
}