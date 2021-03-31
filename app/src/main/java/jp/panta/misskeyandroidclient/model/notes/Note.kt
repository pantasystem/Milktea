package jp.panta.misskeyandroidclient.model.notes

import jp.panta.misskeyandroidclient.model.Entity
import jp.panta.misskeyandroidclient.model.EntityId
import jp.panta.misskeyandroidclient.model.auth.custom.App
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.model.notes.poll.Poll
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionCount
import jp.panta.misskeyandroidclient.model.users.User
import java.util.*

data class Note(
    val id: Id,
    val createdAt: Date,
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
    val files: List<FileProperty>?,
    val poll: Poll?,
    val myReaction: String?,


    val app: App?,
    var instanceUpdatedAt: Date = Date()
) : Entity{

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
        return !(text == null && files.isNullOrEmpty() && poll == null)
    }
}

sealed class NoteRelation {
    abstract val note: Note
    abstract val user: User
    abstract val reply: NoteRelation?
    abstract val renote: NoteRelation?

    data class Normal(
        override val note: Note,
        override val user: User,
        override val renote: NoteRelation?,
        override val reply: NoteRelation?
    ) : NoteRelation()

    data class Featured(
        override val note: Note,
        override val user: User,
        override val renote: NoteRelation?,
        override val reply: NoteRelation?,
        val featuredId: String
    ) : NoteRelation()

    data class Promotion(
        override val note: Note,
        override val user: User,
        override val renote: NoteRelation?,
        override val reply: NoteRelation?,
        val promotionId: String
    ) : NoteRelation()
}
