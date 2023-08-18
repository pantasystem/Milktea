package net.pantasystem.milktea.model.note.repost

import kotlinx.coroutines.coroutineScope
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.UseCase
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.ap.ApResolverService
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.NoteRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateRenoteMultipleAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val noteRepository: NoteRepository,
    private val checkCanRepostService: CheckCanRepostService,
    private val apResolverService: ApResolverService,
) : UseCase {

    suspend operator fun invoke(
        noteId: Note.Id,
        accountIds: List<Long>,
    ): Result<List<Result<Note>>> = runCancellableCatching {
        coroutineScope {
            val accounts = accountIds.map {
                accountRepository.get(it).getOrThrow()
            }
            val note = recursiveSearchHasContentNote(noteId).getOrThrow()
            accounts.map {
                resolveAndRenote(note, it.accountId)
            }
        }
    }

    private suspend fun resolveAndRenote(sourceNote: Note, accountId: Long): Result<Note> =
        runCancellableCatching {
            val account = accountRepository.get(accountId).getOrThrow()
            val relatedSourceNoteAccount =
                accountRepository.get(sourceNote.id.accountId).getOrThrow()
            val relatedNote = if (account.getHost() != relatedSourceNoteAccount.getHost()) {
                apResolverService.resolve(sourceNote.id, accountId).getOrThrow()
            } else {
                noteRepository.find(Note.Id(account.accountId, sourceNote.id.noteId)).getOrThrow()
            }

            renote(relatedNote).getOrThrow()
        }


    private suspend fun renote(note: Note): Result<Note> =
        runCancellableCatching {
            if (checkCanRepostService
                    .canRepost(note.id)
                    .getOrElse { false }
            ) {
                noteRepository.renote(note.id).getOrThrow()
            } else {
                throw IllegalArgumentException()
            }
        }

    private suspend fun recursiveSearchHasContentNote(noteId: Note.Id): Result<Note> =
        runCancellableCatching {
            val note = noteRepository.find(noteId).getOrThrow()
            if (note.hasContent()) {
                note
            } else {
                recursiveSearchHasContentNote(note.renoteId!!).getOrThrow()
            }
        }
}

