package net.pantasystem.milktea.model.notes

import net.pantasystem.milktea.model.UseCase
import javax.inject.Inject


class DeleteNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
): UseCase {

    suspend operator fun invoke(noteId: Note.Id): Result<Note> {
        return noteRepository.delete(noteId)
    }
}