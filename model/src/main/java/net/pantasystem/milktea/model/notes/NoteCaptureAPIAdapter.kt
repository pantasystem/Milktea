package net.pantasystem.milktea.model.notes

import kotlinx.coroutines.flow.Flow

interface NoteCaptureAPIAdapter {
    fun capture(id: Note.Id): Flow<NoteDataSource.Event>
}