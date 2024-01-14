package net.pantasystem.milktea.model.note

import net.pantasystem.milktea.common.mapCancellableCatching
import net.pantasystem.milktea.model.UseCase
import net.pantasystem.milktea.model.account.AccountRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetShareNoteUrlUseCase @Inject constructor(
    private val noteService: NoteService,
    private val accountRepository: AccountRepository,
) : UseCase {
    suspend operator fun invoke(noteId: Note.Id): Result<String> {
        return noteService.findHasContentNote(noteId).mapCancellableCatching { note ->
            note.getOriginUrl(
                accountRepository.get(noteId.accountId).getOrThrow()
            )
        }
    }

}