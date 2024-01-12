package net.pantasystem.milktea.model.note

import net.pantasystem.milktea.common.mapCancellableCatching
import net.pantasystem.milktea.model.UseCase
import net.pantasystem.milktea.model.account.AccountRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetShareNoteUrlUseCase @Inject constructor(
    private val recursiveSearchHasContentNoteUseCase: RecursiveSearchHasContentNoteUseCase,
    private val accountRepository: AccountRepository,
) : UseCase {
    suspend operator fun invoke(noteId: Note.Id): Result<String> {
        return recursiveSearchHasContentNoteUseCase(noteId).mapCancellableCatching { note ->
            note.getOriginUrl(
                accountRepository.get(noteId.accountId).getOrThrow()
            )
        }
    }

}