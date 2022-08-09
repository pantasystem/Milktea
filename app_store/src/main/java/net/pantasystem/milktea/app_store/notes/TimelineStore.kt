package net.pantasystem.milktea.app_store.notes

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
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


    suspend fun loadPrevious(): Result<Unit>
    suspend fun loadFuture(): Result<Unit>
    suspend fun clear()
    fun onReceiveNote(noteId: Note.Id)

    fun latestReceiveNoteId(): Note.Id?
}