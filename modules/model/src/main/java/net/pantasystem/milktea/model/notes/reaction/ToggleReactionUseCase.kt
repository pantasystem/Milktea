package net.pantasystem.milktea.model.notes.reaction

import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.UseCase
import net.pantasystem.milktea.model.account.GetAccount
import net.pantasystem.milktea.model.emoji.CustomEmojiRepository
import net.pantasystem.milktea.model.instance.InstanceInfoService
import net.pantasystem.milktea.model.instance.InstanceInfoType
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
    private val instanceInfoService: InstanceInfoService,
    private val customEmojiRepository: CustomEmojiRepository,
    private val checkEmoji: CheckEmoji,
) : UseCase {

    suspend operator fun invoke(noteId: Note.Id, reaction: String): Result<Unit> {
        return runCancellableCatching {
            val account = getAccount.get(noteId.accountId)
            val instanceType = instanceInfoService.find(account.normalizedInstanceUri).getOrThrow()
            val reactionObj = Reaction(reaction)
            val sendReaction = if (checkEmoji.checkEmoji(reaction)) {
                reaction
            } else if (LegacyReaction.reactionMap.containsKey(reaction)) {
                requireNotNull(LegacyReaction.reactionMap[reaction])
            } else {
                when(instanceType) {
                    is InstanceInfoType.Mastodon -> {
                        val maxCount = instanceType.maxReactionsPerAccount
                        if (maxCount < 1) {
                            return@runCancellableCatching
                        }

                        reactionObj.getNameAndHost()
                    }
                    is InstanceInfoType.Misskey -> {
                        val name = reactionObj.getName()
                            ?: return@runCancellableCatching
                        val hitEmojis = customEmojiRepository.findByName(account.getHost(), name).getOrThrow()
                        val hitEmoji = hitEmojis.firstOrNull()
                        if (hitEmoji == null) {
                            "👍"
                        } else {
                            reaction
                        }
                    }
                }
            }

            val note = noteRepository.find(noteId).getOrThrow()

            val isReacted = note.reactionCounts.any {
                it.reaction == reaction && it.me
            }
            // 同一のリアクションを選択した場合は解除して終了する
            if (isReacted) {
                reactionRepository.delete(noteId).getOrThrow()
                return@runCancellableCatching
            }

            if (instanceType.maxReactionsPerAccount == 1) {
                // 他にリアクション済みのリアクションがあればそれを解除する
                note.reactionCounts.firstOrNull {
                    it.me
                }?.let {
                    reactionRepository.delete(noteId).getOrThrow()
                }
            } else {
                // リアクション可能な件数をオーバーしてしまっていた場合はキャンセルする
                if (getMyReactionCount(note) >= instanceType.maxReactionsPerAccount) {
                    return@runCancellableCatching
                }
            }

            if (reactionRepository.create(CreateReaction(noteId, sendReaction)).getOrThrow()) {
                reactionHistoryRepository.create(
                    ReactionHistory(
                        sendReaction,
                        account.normalizedInstanceUri
                    )
                )
            }
        }
    }

    internal fun getMyReactionCount(note: Note): Int {
        return note.reactionCounts.count {
            it.me
        }
    }

}