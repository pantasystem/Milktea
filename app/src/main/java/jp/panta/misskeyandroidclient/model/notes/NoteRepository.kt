package jp.panta.misskeyandroidclient.model.notes

import jp.panta.misskeyandroidclient.model.notes.reaction.CreateReaction
import jp.panta.misskeyandroidclient.util.State
import jp.panta.misskeyandroidclient.util.StateContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface NoteRepository {

    suspend fun delete(noteId: Note.Id): Boolean

    suspend fun create(createNote: CreateNote): Note

    suspend fun find(noteId: Note.Id): Note

    /**
     * @param createReaction リアクションを作成するための値を表す。
     * リアクションに成功するとtrueが返される。
     * 既に選択されているリアクションが選択されている時はtoggleされる。
     * 既に選択されているリアクションとは異なるリアクションを選択した時は解除してリアクションが作成される。
     */
    suspend fun toggleReaction(createReaction: CreateReaction): Boolean

    suspend fun unreaction(noteId: Note.Id): Boolean
}