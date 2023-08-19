package net.pantasystem.milktea.model.note

import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.model.note.poll.Poll

interface NoteRepository {

    suspend fun delete(noteId: Note.Id): Result<Note>

    suspend fun create(createNote: CreateNote): Result<Note>

    suspend fun renote(noteId: Note.Id): Result<Note>

    suspend fun unrenote(noteId: Note.Id): Result<Unit>

    suspend fun find(noteId: Note.Id): Result<Note>

    suspend fun findIn(noteIds: List<Note.Id>): List<Note>

    suspend fun vote(noteId: Note.Id, choice: Poll.Choice): Result<Unit>

//    suspend fun syncConversation(noteId: Note.Id): Result<Unit>
//
//    suspend fun syncChildren(noteId: Note.Id): Result<Unit>

    suspend fun syncThreadContext(noteId: Note.Id): Result<Unit>

    suspend fun sync(noteId: Note.Id): Result<Unit>

    suspend fun createThreadMute(noteId: Note.Id): Result<Unit>

    suspend fun deleteThreadMute(noteId: Note.Id): Result<Unit>

    suspend fun findNoteState(noteId: Note.Id): Result<NoteState>

    fun observeIn(noteIds: List<Note.Id>): Flow<List<Note>>

    fun observeOne(noteId: Note.Id): Flow<Note?>

    fun observeThreadContext(noteId: Note.Id): Flow<NoteThreadContext>

}