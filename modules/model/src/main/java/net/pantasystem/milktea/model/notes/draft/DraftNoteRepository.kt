package net.pantasystem.milktea.model.notes.draft

interface DraftNoteRepository {

    suspend fun save(draftNote: DraftNote): Result<DraftNote>
    suspend fun delete(draftNoteId: Long): Result<Unit>
    suspend fun findOne(draftNoteId: Long): Result<DraftNote>
}