package jp.panta.misskeyandroidclient.model.notes

import kotlinx.coroutines.flow.Flow


/**
 * アクションと状態の保持を責務とする
 */
interface NoteModel {

    suspend fun get(noteId: Note.Id): Note?



    suspend fun delete(note: Note)
    suspend fun reaction(reaction: String, reactionTo: Note)
    suspend fun unreaction(reaction: String, unreactionTo: Note)
    suspend fun create(createNote: CreateNote)

}