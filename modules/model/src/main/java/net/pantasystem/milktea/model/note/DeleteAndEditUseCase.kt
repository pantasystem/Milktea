package net.pantasystem.milktea.model.note

import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.UseCase
import net.pantasystem.milktea.model.note.draft.DraftNote
import net.pantasystem.milktea.model.note.draft.DraftNoteRepository
import net.pantasystem.milktea.model.note.draft.toDraftNote
import javax.inject.Inject

class DeleteAndEditUseCase @Inject constructor(
    val noteRepository: NoteRepository,
    val noteRelationGetter: NoteRelationGetter,
    val draftNoteRepository: DraftNoteRepository,
) : UseCase {

    suspend operator fun invoke(id: Note.Id): Result<DraftNote> = runCancellableCatching {
        val relation = noteRelationGetter.get(id).getOrThrow()
        val result = noteRepository.delete(id).getOrThrow()
        val draftNote = requireNotNull(relation).copy(note = result).toDraftNote()
        draftNoteRepository.save(draftNote).getOrThrow()
    }
}
