package net.pantasystem.milktea.model.note.poll

import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.UseCase
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.NoteRepository
import javax.inject.Inject

class VoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
): UseCase {

    suspend operator fun invoke(noteId: Note.Id, choice: Poll.Choice): Result<Note> = runCancellableCatching{
        val note = noteRepository.find(noteId).getOrThrow()
        if (note.poll == null) throw IllegalArgumentException("Note is not poll")
        if (!note.poll.canVote) throw IllegalArgumentException("Poll is closed")
        noteRepository.vote(noteId, choice).getOrThrow()
        noteRepository.find(noteId).getOrThrow()
    }
}