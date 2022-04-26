package net.pantasystem.milktea.model.notes.reaction

import com.vdurmont.emoji.EmojiManager
import net.pantasystem.milktea.model.UseCase
import net.pantasystem.milktea.model.account.GetAccount
import net.pantasystem.milktea.model.instance.FetchMeta
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteRepository
import net.pantasystem.milktea.model.notes.reaction.history.ReactionHistory
import net.pantasystem.milktea.model.notes.reaction.history.ReactionHistoryDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToggleReactionUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
    private val reactionHistoryDao: ReactionHistoryDao,
    private val getAccount: GetAccount,
    private val fetchMeta: FetchMeta,
) : UseCase {

    suspend operator fun invoke(noteId: Note.Id, reaction: String): Result<Unit> {
        return runCatching {
            val account = getAccount.get(noteId.accountId)
            val reactionObj = Reaction(reaction)
            val sendReaction =
                if (EmojiManager.isEmoji(reaction) || fetchMeta.fetch(account.instanceDomain)
                        .isOwnEmojiBy(reactionObj)
                ) {
                    reaction
                } else {
                    "👍"
                }
            val note = noteRepository.find(noteId)
            if (note.myReaction.isNullOrBlank()) {
                if (noteRepository.reaction(CreateReaction(noteId, sendReaction))) {
                    reactionHistoryDao.insert(ReactionHistory(sendReaction, account.instanceDomain))
                }
            } else if (note.myReaction != sendReaction) {
                noteRepository.unreaction(noteId)
                noteRepository.reaction(CreateReaction(noteId, sendReaction))
            } else {
                noteRepository.unreaction(noteId)
            }
        }
    }
}