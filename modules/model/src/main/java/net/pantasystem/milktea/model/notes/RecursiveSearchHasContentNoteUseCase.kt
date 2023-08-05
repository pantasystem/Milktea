package net.pantasystem.milktea.model.notes

import net.pantasystem.milktea.common.mapCancellableCatching
import net.pantasystem.milktea.model.UseCase
import javax.inject.Inject

class RecursiveSearchHasContentNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
) : UseCase {

    suspend operator fun invoke(noteId: Note.Id): Result<Note> {
        return noteRepository.find(noteId).mapCancellableCatching { note ->
            if (note.hasContent()) {
                note
            } else {
                invoke(requireNotNull(note.renoteId)).getOrThrow()
            }
        }
    }
}