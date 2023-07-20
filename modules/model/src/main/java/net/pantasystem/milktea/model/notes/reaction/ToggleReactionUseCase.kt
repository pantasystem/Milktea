package net.pantasystem.milktea.model.notes.reaction

import net.pantasystem.milktea.common.mapCancellableCatching
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.UseCase
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.GetAccount
import net.pantasystem.milktea.model.emoji.CustomEmojiRepository
import net.pantasystem.milktea.model.instance.InstanceInfoService
import net.pantasystem.milktea.model.instance.InstanceInfoType
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteRepository
import net.pantasystem.milktea.model.notes.reaction.history.ReactionHistory
import net.pantasystem.milktea.model.notes.reaction.history.ReactionHistoryRepository
import net.pantasystem.milktea.model.user.UserRepository
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
    private val userRepository: UserRepository,
) : UseCase {

    suspend operator fun invoke(noteId: Note.Id, reaction: String): Result<Unit> {
        return runCancellableCatching {
            val account = getAccount.get(noteId.accountId)
            val instanceType = instanceInfoService.find(account.normalizedInstanceUri).getOrThrow()
            val sendReaction = getSendReaction(instanceType, account, reaction)
                ?: return@runCancellableCatching
            val note = noteRepository.find(noteId).getOrThrow()

            // 同一のリアクションを選択した場合は解除して終了する
            if (note.isReactedReaction(reaction)) {
                reactionRepository.delete(
                    DeleteReaction(
                        noteId,
                        reaction
                    )
                ).getOrThrow()
                return@runCancellableCatching
            }

            if (!instanceType.canMultipleReaction) {
                // 他にリアクション済みのリアクションがあればそれを解除する
                note.reactionCounts.firstOrNull {
                    it.me
                }?.let {
                    reactionRepository.delete(
                        DeleteReaction(
                            noteId,
                            it.reaction
                        )
                    ).getOrThrow()
                }
            } else {
                // リアクション可能な件数をオーバーしてしまっていた場合はキャンセルする
                if (note.getMyReactionCount() >= instanceType.maxReactionsPerAccount) {
                    return@runCancellableCatching
                }
            }

            if (reactionRepository.create(CreateReaction(noteId, sendReaction)).getOrThrow()) {
                reactionHistoryRepository.create(
                    ReactionHistory(
                        reaction = sendReaction,
                        instanceDomain = account.normalizedInstanceUri,
                        accountId = account.accountId,
                        targetPostId = noteId.noteId,
                        targetUserId = note.userId.id,
                    )
                )
            }

            // NOTE: Suggestionを表示するためにユーザのデータが必要になるので、キャッシュを更新しておく
            userRepository.sync(note.userId).getOrThrow()
        }.mapCancellableCatching {
            noteRepository.sync(noteId).getOrThrow()
        }
    }


    internal suspend fun getSendReaction(
        instanceType: InstanceInfoType,
        account: Account,
        reaction: String,
    ): String? {
        val reactionObj = Reaction(reaction)
        return if (checkEmoji.checkEmoji(reaction)) {
            reaction
        } else if (LegacyReaction.reactionMap.containsKey(reaction)) {
            requireNotNull(LegacyReaction.reactionMap[reaction])
        } else {
            when (instanceType) {
                is InstanceInfoType.Mastodon -> {
                    val maxCount = instanceType.maxReactionsPerAccount
                    if (maxCount < 1) {
                        return null
                    }

                    reactionObj.getNameAndHost()
                }
                is InstanceInfoType.Firefish -> {
                    val maxCount = instanceType.maxReactionsPerAccount
                    if (maxCount < 1) {
                        return null
                    }

                    reactionObj.getNameAndHost()
                }
                is InstanceInfoType.Pleroma -> {
                    val maxCount = instanceType.maxReactionsPerAccount
                    if (maxCount < 1) {
                        return null
                    }

                    // TODO: ユニコード絵文字の場合コロンは不要かもしれない
                    ":${reactionObj.getNameAndHost()}:"
                }
                is InstanceInfoType.Misskey -> {
                    val name = reactionObj.getName()
                        ?: return null
                    val hitEmojis =
                        customEmojiRepository.findByName(account.getHost(), name).getOrThrow()
                    val hitEmoji = hitEmojis.firstOrNull()
                    if (hitEmoji == null) {
                        "👍"
                    } else {
                        reaction
                    }
                }
            }
        }
    }

}