package net.pantasystem.milktea.model.notes

import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.UseCase
import net.pantasystem.milktea.model.notes.draft.DraftNote
import net.pantasystem.milktea.model.notes.draft.DraftNoteRepository
import net.pantasystem.milktea.model.notes.draft.toDraftNote
import javax.inject.Inject

class DeleteAndEditUseCase @Inject constructor(
    val noteRepository: NoteRepository,
    val noteRelationGetter: NoteRelationGetter,
    val draftNoteRepository: DraftNoteRepository,
) : UseCase {

    suspend operator fun invoke(id: Note.Id): Result<DraftNote> = runCancellableCatching {
        val relation = noteRelationGetter.get(id).getOrThrow()
        val result = noteRepository.delete(id).getOrThrow()
        val draftNote = when(val r = requireNotNull(relation)) {
            is NoteRelation.Normal -> r.copy(
                note = result
            )
        }.toDraftNote()
        draftNoteRepository.save(draftNote).getOrThrow()
    }
}
