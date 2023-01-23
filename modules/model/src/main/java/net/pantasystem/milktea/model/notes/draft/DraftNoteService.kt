package net.pantasystem.milktea.model.notes.draft

import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.model.notes.CreateNote


interface DraftNoteService {
    fun getDraftNoteSavedEventBy(accountId: Long): Flow<DraftNoteSavedEvent>
    suspend fun save(createNote: CreateNote): Result<DraftNote>
    suspend fun save(draftNoteFile: DraftNoteFile): Result<DraftNoteFile>
}

sealed interface DraftNoteSavedEvent {
    data class Failed(val createNote: CreateNote, val throwable: Throwable) : DraftNoteSavedEvent
    data class Success(val draftNote: DraftNote) : DraftNoteSavedEvent
}