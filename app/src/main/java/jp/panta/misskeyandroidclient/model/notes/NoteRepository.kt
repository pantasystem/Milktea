package jp.panta.misskeyandroidclient.model.notes

interface NoteRepository {

    suspend fun delete(noteId: Note.Id): Boolean

    suspend fun create(createNote: CreateNote): Note

    suspend fun find(noteId: Note.Id): Note
}