package net.pantasystem.milktea.app_store.notes

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.datetime.Instant
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteRelation

interface TimelineStore {
    interface Factory {
        fun create(pageable: Pageable, coroutineScope: CoroutineScope, getAccount: suspend () -> Account): TimelineStore
    }

    val timelineState: Flow<PageableState<List<Note.Id>>>
    val relatedNotes: Flow<PageableState<List<NoteRelation>>>
    val receiveNoteQueue: SharedFlow<Note.Id>
    val isActiveStreaming: Boolean


    suspend fun loadPrevious(): Result<Unit>
    suspend fun loadFuture(): Result<Unit>

    /**
     * @param initialLoadQuery コンテンツが空の状態の時にloadPreviousを呼び出した時に
     * このパラメーターの日時以降の投稿を取りに行こうとする
     */
    suspend fun clear(initialLoadQuery: InitialLoadQuery?)

    /**
     * Stream API経由で関連するチャンネルのノートを受信した時のハンドラー
     */
    fun onReceiveNote(noteId: Note.Id)

    fun latestReceiveNoteId(): Note.Id?

    fun suspendStreaming()

    suspend fun releaseUnusedPages(position: Int, offset: Int = 50)

}

sealed interface InitialLoadQuery {
    data class UntilId(val noteId: Note.Id) : InitialLoadQuery
    data class UntilDate(val date: Instant) : InitialLoadQuery
}