package net.pantasystem.milktea.model.ap

import net.pantasystem.milktea.common.mapCancellableCatching
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteRepository
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject

class ApResolverService @Inject constructor(
    private val apResolverRepository: ApResolverRepository,
    private val noteRepository: NoteRepository,
    private val userRepository: UserRepository,
    private val accountRepository: AccountRepository,
) {

    suspend fun resolve(noteId: Note.Id, resolveToAccountId: Long): Result<Note> {
        return noteRepository.find(noteId).mapCancellableCatching { note ->
            val host = userRepository.find(note.userId).host
            val noteUri = note.uri ?: "https://$host/notes/${note.id.noteId}"
            apResolverRepository.resolve(resolveToAccountId, noteUri).mapCancellableCatching {
                when (it) {
                    is ApResolver.TypeNote -> it.note
                    is ApResolver.TypeUser -> throw IllegalStateException("Cannot resolve user")
                }
            }.getOrThrow()
        }
    }

    suspend fun resolve(userId: User.Id, resolveToAccountId: Long): Result<User> = runCancellableCatching {
        val user = (userRepository.find(userId, true) as User.Detail)
        val resolveAccount = accountRepository.get(resolveToAccountId).getOrThrow()
        if (resolveAccount.getHost() == user.host) {
            return@runCancellableCatching userRepository.findByUserName(resolveToAccountId, user.userName, user.host)
        }
        val uri = user.getRemoteProfileUrl(
            accountRepository.get(userId.accountId).getOrThrow()
        )
        apResolverRepository.resolve(resolveToAccountId, uri).mapCancellableCatching {
            when (it) {
                is ApResolver.TypeNote -> throw IllegalStateException("Cannot resolve note")
                is ApResolver.TypeUser -> it.user
            }
        }.getOrThrow()
    }
}