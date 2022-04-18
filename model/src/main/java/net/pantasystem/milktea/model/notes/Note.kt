package net.pantasystem.milktea.model.notes

import net.pantasystem.milktea.model.Entity
import net.pantasystem.milktea.model.EntityId
import kotlinx.datetime.Instant
import net.pantasystem.milktea.model.app.AppType
import net.pantasystem.milktea.model.notes.poll.Poll
import net.pantasystem.milktea.model.notes.reaction.ReactionCount
import java.util.*
import java.io.Serializable as JSerializable

data class Note(
    val id: Id,
    val createdAt: Instant,
    val text: String?,
    val cw: String?,
    val userId: net.pantasystem.milktea.model.user.User.Id,

    val replyId: Id?,

    val renoteId: Id?,

    val viaMobile: Boolean?,
    val visibility: Visibility,
    val localOnly: Boolean?,

    val visibleUserIds: List<net.pantasystem.milktea.model.user.User.Id>?,

    val url: String?,
    val uri: String?,
    val renoteCount: Int,
    val reactionCounts: List<ReactionCount>,
    val emojis: List<net.pantasystem.milktea.model.emoji.Emoji>?,
    val repliesCount: Int,
    val fileIds: List<net.pantasystem.milktea.model.drive.FileProperty.Id>?,
    val poll: Poll?,
    val myReaction: String?,


    val app: AppType.Misskey?,
    val channelId: net.pantasystem.milktea.model.channel.Channel.Id?,
    var instanceUpdatedAt: Date = Date()
) : Entity {

    data class Id(
        val accountId: Long,
        val noteId: String
    ) : EntityId

    fun updated(){
        this.instanceUpdatedAt = Date()
    }

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

    /**
     * ファイル、投票、テキストなどのコンテンツを持っているか
     */
    fun hasContent(): Boolean {
        return !(text == null && fileIds.isNullOrEmpty() && poll == null)
    }
}

sealed class NoteRelation : JSerializable{
    abstract val note: Note
    abstract val user: net.pantasystem.milktea.model.user.User
    abstract val reply: NoteRelation?
    abstract val renote: NoteRelation?
    abstract val files: List<net.pantasystem.milktea.model.drive.FileProperty>?

    data class Normal(
        override val note: Note,
        override val user: net.pantasystem.milktea.model.user.User,
        override val renote: NoteRelation?,
        override val reply: NoteRelation?,
        override val files: List<net.pantasystem.milktea.model.drive.FileProperty>?
    ) : NoteRelation()

    data class Featured(
        override val note: Note,
        override val user: net.pantasystem.milktea.model.user.User,
        override val renote: NoteRelation?,
        override val reply: NoteRelation?,
        override val files: List<net.pantasystem.milktea.model.drive.FileProperty>?,
        val featuredId: String
    ) : NoteRelation()

    data class Promotion(
        override val note: Note,
        override val user: net.pantasystem.milktea.model.user.User,
        override val renote: NoteRelation?,
        override val reply: NoteRelation?,
        override val files: List<net.pantasystem.milktea.model.drive.FileProperty>?,
        val promotionId: String
    ) : NoteRelation()
}
