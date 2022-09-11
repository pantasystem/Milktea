package net.pantasystem.milktea.model.notes.renote

import net.pantasystem.milktea.model.UseCase
import net.pantasystem.milktea.model.account.GetAccount
import net.pantasystem.milktea.model.notes.CreateNote
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteRepository
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateRenoteUseCase @Inject constructor(
    val noteRepository: NoteRepository,
    val getAccount: GetAccount,
) : UseCase {

    suspend operator fun invoke(noteId: Note.Id): Result<Note> {
        return runCatching {
            val note = recursiveSearchHasContentNote(noteId).getOrThrow()
            val account = getAccount.get(noteId.accountId)
            if (note.canRenote(User.Id(accountId = account.accountId, id = account.remoteId))) {
                noteRepository.create(CreateNote(
                    author = account,
                    text = null,
                    visibility = note.visibility,
                    renoteId = note.id,
                )).getOrThrow()
            } else {
                throw IllegalArgumentException()
            }
        }
    }

    private suspend fun recursiveSearchHasContentNote(noteId: Note.Id): Result<Note> = runCatching {
        val note = noteRepository.find(noteId).getOrThrow()
        if (note.hasContent()) {
            note
        } else {
            recursiveSearchHasContentNote(note.renoteId!!).getOrThrow()
        }
    }
}