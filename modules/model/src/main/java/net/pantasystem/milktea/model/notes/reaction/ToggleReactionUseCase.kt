package net.pantasystem.milktea.model.notes.reaction

import net.pantasystem.milktea.model.UseCase
import net.pantasystem.milktea.model.account.GetAccount
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteRepository
import net.pantasystem.milktea.model.notes.reaction.history.ReactionHistory
import net.pantasystem.milktea.model.notes.reaction.history.ReactionHistoryRepository
import javax.inject.Inject
import javax.inject.Singleton

interface CheckEmoji {
    suspend fun checkEmoji(char: CharSequence): Boolean
}


@Singleton
class ToggleReactionUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
    private val reactionHistoryRepository: ReactionHistoryRepository,
    private val getAccount: GetAccount,
    private val metaRepository: MetaRepository,
    private val checkEmoji: CheckEmoji,
) : UseCase {

    suspend operator fun invoke(noteId: Note.Id, reaction: String): Result<Unit> {
        return runCatching {
            val account = getAccount.get(noteId.accountId)
            val reactionObj = Reaction(reaction)
            val sendReaction =
                if (
                    checkEmoji.checkEmoji(reaction)
                    || metaRepository.find(account.instanceDomain).getOrThrow()
                        .isOwnEmojiBy(reactionObj)
                    || LegacyReaction.reactionMap.containsKey(reaction)
                ) {
                    reaction
                } else {
                    "👍"
                }
            val note = noteRepository.find(noteId).getOrThrow()
            if (note.myReaction.isNullOrBlank()) {
                if (noteRepository.reaction(CreateReaction(noteId, sendReaction)).getOrThrow()) {
                    reactionHistoryRepository.create(ReactionHistory(sendReaction, account.instanceDomain))
                }
            } else if (note.myReaction != sendReaction) {
                noteRepository.unreaction(noteId).getOrThrow()
                if (noteRepository.reaction(CreateReaction(noteId, sendReaction)).getOrThrow()) {
                    reactionHistoryRepository.create(ReactionHistory(sendReaction, account.instanceDomain))
                }
            } else {
                noteRepository.unreaction(noteId).getOrThrow()
            }
        }
    }
}