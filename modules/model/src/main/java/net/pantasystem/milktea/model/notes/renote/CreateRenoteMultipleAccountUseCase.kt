package net.pantasystem.milktea.model.notes.renote

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import net.pantasystem.milktea.model.UseCase
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.ap.ApResolver
import net.pantasystem.milktea.model.ap.ApResolverRepository
import net.pantasystem.milktea.model.notes.CreateNote
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteRepository
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreateRenoteMultipleAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val apResolverRepository: ApResolverRepository,
    private val userRepository: UserRepository,
    private val noteRepository: NoteRepository,
) : UseCase {

    suspend operator fun invoke(
        noteId: Note.Id,
        accountIds: List<Long>
    ): Result<List<Result<Note>>> = runCatching {
        coroutineScope {

            val relatedAccount = accountRepository.get(noteId.accountId).getOrThrow()

            val accounts = accountIds.map {
                accountRepository.get(it).getOrThrow()
            }
            val note = recursiveSearchHasContentNote(noteId).getOrThrow()
            val user = userRepository.find(note.userId)

            val noteUri = note.uri ?: "https://${user.host}/notes/${note.id.noteId}"

            // NOTE: 元々ノートに関連付けされていたアカウントと同一ホストでなければIdの解決を行う必要がある。
            val accountWithResolvedNotes = resolveAccounts(noteUri, accounts.filter {
                it.getHost() != relatedAccount.getHost()
            })


            val sameInstanceAccountWithNotes = accounts.filter {
                it.getHost() == relatedAccount.getHost()
            }.map { account ->
                async {
                    // NOTE: アカウントに関連づけられたNoteを探索する
                    noteRepository.find(Note.Id(account.accountId, note.id.noteId)).getOrThrow()
                        .let {
                            account to it
                        }
                }
            }.awaitAll()

            (accountWithResolvedNotes + sameInstanceAccountWithNotes).map { (account, note) ->
                async {
                    renote(account, note)
                }
            }.awaitAll()
        }


    }

    private suspend fun resolveAccounts(
        noteUri: String,
        accounts: List<Account>
    ): List<Pair<Account, Note>> {
        return coroutineScope {
            accounts.map { account ->
                async {
                    apResolverRepository.resolve(account.accountId, noteUri).getOrThrow().let {
                        account to (it as ApResolver.TypeNote).note
                    }
                }
            }.awaitAll()
        }
    }

    private suspend fun renote(account: Account, note: Note): Result<Note> = runCatching {
        if (note.canRenote(User.Id(account.accountId, account.remoteId))) {
            noteRepository.create(
                CreateNote(
                    author = account,
                    text = null,
                    visibility = note.visibility,
                    renoteId = note.id,
                )
            ).getOrThrow()
        } else {
            throw IllegalArgumentException()
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

