package net.pantasystem.milktea.model.notes.reaction

import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.UseCase
import net.pantasystem.milktea.model.account.GetAccount
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import net.pantasystem.milktea.model.nodeinfo.NodeInfoRepository
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
    private val reactionRepository: ReactionRepository,
    private val reactionHistoryRepository: ReactionHistoryRepository,
    private val getAccount: GetAccount,
    private val metaRepository: MetaRepository,
    private val nodeInfoRepository: NodeInfoRepository,
    private val checkEmoji: CheckEmoji,
) : UseCase {

    suspend operator fun invoke(noteId: Note.Id, reaction: String): Result<Unit> {
        return runCancellableCatching {
            val account = getAccount.get(noteId.accountId)
            val reactionObj = Reaction(reaction)
            val sendReaction =
                if (
                    checkEmoji.checkEmoji(reaction)
                    || metaRepository.find(account.normalizedInstanceUri).getOrThrow()
                        .isOwnEmojiBy(reactionObj)
                ) {
                    reaction
                } else if (LegacyReaction.reactionMap.containsKey(reaction)) {
                    requireNotNull(LegacyReaction.reactionMap[reaction])
                } else if (
                    nodeInfoRepository
                        .find(account.getHost())
                        .getOrThrow().type is NodeInfo.SoftwareType.Mastodon.Fedibird
                ) {
                    Reaction(reaction).getNameAndHost()
                } else {
                    "👍"
                }
            val note = noteRepository.find(noteId).getOrThrow()
            if (note.myReaction.isNullOrBlank()) {
                if (reactionRepository.create(CreateReaction(noteId, sendReaction)).getOrThrow()) {
                    reactionHistoryRepository.create(
                        ReactionHistory(
                            sendReaction,
                            account.normalizedInstanceUri
                        )
                    )
                }
            } else if (note.myReaction != sendReaction) {
                reactionRepository.delete(noteId).getOrThrow()
                if (reactionRepository.create(CreateReaction(noteId, sendReaction)).getOrThrow()) {
                    reactionHistoryRepository.create(
                        ReactionHistory(
                            sendReaction,
                            account.normalizedInstanceUri
                        )
                    )
                }
            } else {
                reactionRepository.delete(noteId).getOrThrow()
            }
        }
    }
}