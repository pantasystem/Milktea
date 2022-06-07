package net.pantasystem.milktea.model.notes

import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.common.PageableState

interface TimelineStore {
    val timelineState: Flow<PageableState<List<Note.Id>>>
    val relatedNotes: Flow<PageableState<List<NoteRelation>>>

    suspend fun loadPrevious(): Result<Unit>
    suspend fun loadFuture(): Result<Unit>

}