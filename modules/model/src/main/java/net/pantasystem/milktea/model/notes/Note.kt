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
import net.pantasystem.milktea.model.notes.reaction.Reaction
import net.pantasystem.milktea.model.notes.reaction.ReactionCount
import net.pantasystem.milktea.model.user.User
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
    val promotionId: String? = null,
    val featuredId: String? = null,
) : Entity {
    data class Id(
        val accountId: Long,
        val noteId: String
    ) : EntityId

    companion object;

    /**
     * 引用リノートであるか
     */
    fun isQuote(): Boolean {
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
        return text != null || !fileIds.isNullOrEmpty() || poll != null
    }

    fun isOwnReaction(reaction: Reaction): Boolean {
        return myReaction != null && myReaction == reaction.getName()
    }

    /**
     * この投稿がRenote可能であるかをチェックしている。
     * 既に取得できた投稿なので少なくともHome, Followers, Specifiedの公開範囲に
     * 入っていることになるので厳密なチェックは行わない。
     */
    fun canRenote(userId: User.Id): Boolean {
        return id.accountId == userId.accountId
                && (visibility is Visibility.Public
                || visibility is Visibility.Home
                || ((visibility is Visibility.Specified || visibility is Visibility.Followers) && this.userId == userId)
                )
    }
}

sealed class NoteRelation : JSerializable {
    abstract val note: Note
    abstract val user: User
    abstract val reply: NoteRelation?
    abstract val renote: NoteRelation?
    abstract val files: List<FileProperty>?

    data class Normal(
        override val note: Note,
        override val user: User,
        override val renote: NoteRelation?,
        override val reply: NoteRelation?,
        override val files: List<FileProperty>?
    ) : NoteRelation()


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
    )
}