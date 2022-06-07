package net.pantasystem.milktea.model.notes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import net.pantasystem.milktea.common.PageableState

interface TimelineStore {
    val timelineState: Flow<PageableState<List<Note.Id>>>
    val relatedNotes: Flow<PageableState<List<NoteRelation>>>
    val receiveNoteQueue: SharedFlow<Note.Id>


    suspend fun loadPrevious(): Result<Unit>
    suspend fun loadFuture(): Result<Unit>
    suspend fun clear()
    fun onReceiveNote(noteId: Note.Id)

    fun latestReceiveNoteId(): Note.Id?
}