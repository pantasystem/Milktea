package net.pantasystem.milktea.model.notes

import net.pantasystem.milktea.model.notes.reaction.CreateReaction

interface NoteRepository {

    suspend fun delete(noteId: Note.Id): Boolean

    suspend fun create(createNote: CreateNote): Note

    suspend fun find(noteId: Note.Id): Note

    suspend fun findIn(noteIds: List<Note.Id>): List<Note>

    suspend fun reaction(createReaction: CreateReaction): Boolean

    suspend fun unreaction(noteId: Note.Id): Boolean
}