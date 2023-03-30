package net.pantasystem.milktea.model.notes.reaction

import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteRepository
import javax.inject.Inject

class DeleteReactionsUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
    private val reactionRepository: ReactionRepository,
) {

    suspend operator fun invoke(noteId: Note.Id): Result<Unit> = runCancellableCatching {
        val note = noteRepository.find(noteId).getOrThrow()
        note.reactionCounts.filter {
            it.me
        }.map {
            reactionRepository.delete(DeleteReaction(noteId, it.reaction)).getOrThrow()
        }
        noteRepository.sync(noteId).getOrThrow()
    }
}