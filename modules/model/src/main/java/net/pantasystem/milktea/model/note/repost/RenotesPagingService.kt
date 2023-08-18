package net.pantasystem.milktea.model.note.repost

import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.model.note.Note

interface RenotesPagingService {

    interface Factory {
        fun create(noteId: Note.Id): RenotesPagingService
    }

    val state: Flow<PageableState<List<RenoteType>>>
    suspend fun next()
    suspend fun refresh()
    suspend fun clear()
}
