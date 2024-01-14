package net.pantasystem.milktea.model.note

import net.pantasystem.milktea.common.mapCancellableCatching
import net.pantasystem.milktea.model.UseCase
import javax.inject.Inject

class NoteService @Inject constructor(
    private val noteRepository: NoteRepository,
) : UseCase {

    suspend fun findHasContentNote(noteId: Note.Id): Result<Note> {
        return noteRepository.find(noteId).mapCancellableCatching { note ->
            if (note.hasContent()) {
                note
            } else {
                findHasContentNote(requireNotNull(note.renoteId)).getOrThrow()
            }
        }
    }
}