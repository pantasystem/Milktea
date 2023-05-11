package net.pantasystem.milktea.model.notes.repost

import kotlinx.coroutines.coroutineScope
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.UseCase
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.ap.ApResolver
import net.pantasystem.milktea.model.ap.ApResolverRepository
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteRepository
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateRenoteMultipleAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val apResolverRepository: ApResolverRepository,
    private val userRepository: UserRepository,
    private val noteRepository: NoteRepository,
    private val checkCanRepostService: CheckCanRepostService,
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
            val user = userRepository.find(sourceNote.userId)
            val noteUri = sourceNote.uri ?: "https://${user.host}/notes/${sourceNote.id.noteId}"
            val relatedNote = if (account.getHost() != relatedSourceNoteAccount.getHost()) {
                (apResolverRepository.resolve(accountId, noteUri)
                    .getOrThrow() as ApResolver.TypeNote)
                    .note
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

