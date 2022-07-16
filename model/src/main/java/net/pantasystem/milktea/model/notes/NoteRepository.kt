package net.pantasystem.milktea.model.notes

import net.pantasystem.milktea.model.notes.reaction.CreateReaction

interface NoteRepository {

    suspend fun delete(noteId: Note.Id): Result<Unit>

    suspend fun create(createNote: CreateNote): Result<Note>

    suspend fun find(noteId: Note.Id): Result<Note>

    suspend fun findIn(noteIds: List<Note.Id>): List<Note>

    suspend fun reaction(createReaction: CreateReaction): Boolean

    suspend fun unreaction(noteId: Note.Id): Boolean
}