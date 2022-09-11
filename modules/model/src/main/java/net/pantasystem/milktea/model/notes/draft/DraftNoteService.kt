package net.pantasystem.milktea.model.notes.draft

import net.pantasystem.milktea.model.notes.CreateNote


interface DraftNoteService {

    suspend fun save(createNote: CreateNote): Result<DraftNote>
    suspend fun save(draftNoteFile: DraftNoteFile): Result<DraftNoteFile>
}