package net.pantasystem.milktea.model.note.draft

import kotlinx.coroutines.flow.Flow

interface DraftNoteRepository {

    suspend fun save(draftNote: DraftNote): Result<DraftNote>
    suspend fun delete(draftNoteId: Long): Result<Unit>
    suspend fun findOne(draftNoteId: Long): Result<DraftNote>
    fun observeByAccountId(accountId: Long): Flow<List<DraftNote>>
}