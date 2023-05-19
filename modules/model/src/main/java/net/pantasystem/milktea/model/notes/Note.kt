package net.pantasystem.milktea.model.notes

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import net.pantasystem.milktea.model.Entity
import net.pantasystem.milktea.model.EntityId
import net.pantasystem.milktea.model.app.AppType
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.notes.poll.Poll
import net.pantasystem.milktea.model.notes.reaction.ReactionCount
import net.pantasystem.milktea.model.user.User
import kotlin.math.min
import java.io.Serializable as JSerializable

data class Note(
    val id: Id,
    val createdAt: Instant,
    val text: String?,
    val cw: String?,
    val userId: User.Id,

    val replyId: Id?,

    val renoteId: Id?,

    val viaMobile: Boolean?,
    val visibility: Visibility,
    val localOnly: Boolean?,

    val visibleUserIds: List<User.Id>?,

    val url: String?,
    val uri: String?,
    val renoteCount: Int,
    val reactionCounts: List<ReactionCount>,
    val emojis: List<Emoji>?,
    val repliesCount: Int,
    val fileIds: List<FileProperty.Id>?,
    val poll: Poll?,
    val myReaction: String?,

    val app: AppType.Misskey?,
    val channelId: Channel.Id?,
    val type: Type,
    val maxReactionsPerAccount: Int) : Entity {
    class Id(
        val accountId: Long,
        val noteId: String,
    ) : EntityId {

        private var _hashCode: Int? = null
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Id

            if (accountId != other.accountId) return false
            if (noteId != other.noteId) return false

            return true
        }

        override fun hashCode(): Int {
            if (_hashCode != null) return _hashCode!!
            var result = accountId.hashCode()
            result = 31 * result + noteId.hashCode()
            _hashCode = result
            return result
        }

        override fun toString(): String {
            return "Id(accountId=$accountId, noteId='$noteId')"
        }

    }


    sealed interface Type {
        data class Misskey(
            val channel: SimpleChannelInfo? = null,
            val isAcceptingOnlyLikeReaction: Boolean = false,
            val isNotAcceptingSensitiveReaction: Boolean = false,
        ) : Type {
            data class SimpleChannelInfo(val id: Channel.Id, val name: String)

        }

        data class Mastodon(
            val reblogged: Boolean?,
            val favorited: Boolean?,
            val bookmarked: Boolean?,
            val muted: Boolean?,
            val favoriteCount: Int?,
            val tags: List<Tag>,
            val mentions: List<Mention>,
            val isFedibirdQuote: Boolean,
            val pollId: String?,
            val isSensitive: Boolean?,
            val pureText: String?,
            val isReactionAvailable: Boolean,
        ) : Type {
            data class Tag(
                val name: String,
                val url: String,
            )

            data class Mention(
                val id: String,
                val username: String,
                val url: String,
                val acct: String,
            )
        }
    }

    companion object {
        const val SHORT_REACTION_COUNT_MAX_SIZE = 16
        const val SHORT_RENOTE_REACTION_COUNT_MAX_SIZE = 8
    }

    val isMastodon: Boolean = type is Type.Mastodon
    val isMisskey: Boolean = type is Type.Misskey

    val isSupportEmojiReaction: Boolean =
        type is Type.Misskey || (type is Type.Mastodon && type.isReactionAvailable)

    val isAcceptingOnlyLikeReaction: Boolean =
        type is Type.Misskey && type.isAcceptingOnlyLikeReaction

    val emojiNameMap = emojis?.associateBy {
        it.name
    }

    val isReacted: Boolean = reactionCounts.any {
        it.me
    }

    val canReaction: Boolean = reactionCounts.count {
        it.me
    } < maxReactionsPerAccount

    val reactionsCount = reactionCounts.sumOf {
        it.count
    }

    fun getShortReactionCounts(isRenote: Boolean): List<ReactionCount> {
        return if (isRenote) {
            if (reactionCounts.size <= SHORT_RENOTE_REACTION_COUNT_MAX_SIZE) {
                reactionCounts
            } else {
                reactionCounts.subList(
                    0,
                    min(reactionCounts.size, SHORT_RENOTE_REACTION_COUNT_MAX_SIZE)
                )
            }
        } else {
            if (reactionCounts.size <= SHORT_REACTION_COUNT_MAX_SIZE) {
                reactionCounts
            } else {
                reactionCounts.subList(0, min(reactionCounts.size, SHORT_REACTION_COUNT_MAX_SIZE))
            }
        }
    }

    /**
     * 引用リノートであるか
     */
    fun isQuote(): Boolean {
        if (type is Type.Mastodon && type.isFedibirdQuote) {
            return true
        }
        // NOTE: mastodonには引用が存在しない
        if (isMastodon) {
            return false
        }

        return isRenote() && hasContent()
    }

    /**
     * リノートであるか
     */
    fun isRenote(): Boolean {
        return renoteId != null
    }


    fun isRenoteOnly(): Boolean {
        return isRenote() && !hasContent()
    }

    /**
     * ファイル、投票、テキストなどのコンテンツを持っているか
     */
    fun hasContent(): Boolean {
        if (type is Type.Mastodon && type.isFedibirdQuote) {
            return true
        }

        if (isMastodon && isRenote()) {
            return false
        }
        return text != null || !fileIds.isNullOrEmpty() || poll != null
    }

    fun isReactedReaction(reaction: String): Boolean {
        return reactionCounts.any {
            it.reaction == reaction && it.me
        }
    }

    fun getMyReactionCount(): Int {
        return reactionCounts.count {
            it.me
        }
    }

    /**
     * この投稿がRenote可能であるかをチェックしている。
     * 既に取得できた投稿なので少なくともHome, Followers, Specifiedの公開範囲に
     * 入っていることになるので厳密なチェックは行わない。
     */
    fun canRenote(userId: User.Id): Boolean {
        return when (type) {
            is Type.Mastodon -> visibility is Visibility.Public
                    || visibility is Visibility.Home
            is Type.Misskey -> id.accountId == userId.accountId
                    && (visibility is Visibility.Public
                    || visibility is Visibility.Home
                    || ((visibility is Visibility.Specified || visibility is Visibility.Followers) && this.userId == userId)
                    )
        }

    }
}

data class NoteRelation(
    val note: Note,
    val user: User,
    val renote: NoteRelation?,
    val reply: NoteRelation?,
    val files: List<FileProperty>?,
) : JSerializable {

    val contentNote: NoteRelation = if (note.isRenote() && !note.hasContent()) {
        renote ?: this
    } else {
        this
    }
}

fun Note.Companion.make(
    id: Note.Id,
    userId: User.Id,
    createdAt: Instant = Clock.System.now(),
    text: String? = null,
    cw: String? = null,
    replyId: Note.Id? = null,
    renoteId: Note.Id? = null,
    viaMobile: Boolean? = null,
    visibility: Visibility = Visibility.Public(false),
    localOnly: Boolean? = false,
    visibleUserIds: List<User.Id>? = null,
    url: String? = null,
    uri: String? = null,
    renoteCount: Int = 0,
    reactionCounts: List<ReactionCount> = emptyList(),
    emojis: List<Emoji>? = null,
    repliesCount: Int = 0,
    fileIds: List<FileProperty.Id>? = null,
    poll: Poll? = null,
    myReaction: String? = null,
    app: AppType.Misskey? = null,
    channelId: Channel.Id? = null,
    type: Note.Type = Note.Type.Misskey(),
    maxReactionsPerAccount: Int = 1
): Note {
    return Note(
        id = id,
        userId = userId,
        createdAt = createdAt,
        text = text,
        cw = cw,
        replyId = replyId,
        renoteId = renoteId,
        viaMobile = viaMobile,
        visibility = visibility,
        localOnly = localOnly,
        visibleUserIds = visibleUserIds,
        url = url,
        uri = uri,
        renoteCount = renoteCount,
        reactionCounts = reactionCounts,
        emojis = emojis,
        repliesCount = repliesCount,
        fileIds = fileIds,
        poll = poll,
        myReaction = myReaction,
        app = app,
        channelId = channelId,
        type = type,
        maxReactionsPerAccount = maxReactionsPerAccount
    )
}