package net.pantasystem.milktea.model.notes.repost

import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteRepository
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckCanRepostService @Inject constructor(
    val accountRepository: AccountRepository,
    val noteRepository: NoteRepository,
) {

    suspend fun canRepost(noteId: Note.Id): Result<Boolean> = runCancellableCatching {
        val note = noteRepository.find(noteId).getOrThrow()
        val account = accountRepository.get(noteId.accountId).getOrThrow()
        val userId = User.Id(account.accountId, account.remoteId)
        if (note.isRenoteOnly()) {
            return@runCancellableCatching false
        }

        if ((note.type as? Note.Type.Mastodon)?.reblogged == true) {
            return@runCancellableCatching false
        }

        if (userId == note.userId) {
            return@runCancellableCatching true
        }

        if (!note.canRenote(userId)) {
            return@runCancellableCatching false
        }

        true
    }
}