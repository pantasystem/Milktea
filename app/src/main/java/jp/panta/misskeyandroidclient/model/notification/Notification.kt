package jp.panta.misskeyandroidclient.model.notification

import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.api.users.UserDTO
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.serializations.DateSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*



sealed class Notification {
    abstract val id: Id
    abstract val userId: User.Id
    abstract val createdAt: Date
    abstract val isRead: Boolean

    data class Id(
        val accountId: Long,
        val notificationId: String
    ) : java.io.Serializable
}


interface HasNote {
    val noteId: Note.Id
}

data class FollowNotification(
    override val id: Id,

    override val createdAt: Date,
    override val userId: User.Id,
    override val isRead: Boolean
) : Notification()

data class FollowRequestAcceptedNotification(
    override val id: Id,

    override val createdAt: Date,
    override val userId: User.Id,
    override val isRead: Boolean

) : Notification()

data class ReceiveFollowRequestNotification(
    override val id: Id,

    override val createdAt: Date,
    override val userId: User.Id,
    override val isRead: Boolean

) : Notification()

data class MentionNotification(
    override val id: Id,

    override val createdAt: Date,
    override val userId: User.Id,
    override val noteId: Note.Id,
    override val isRead: Boolean

) : Notification(), HasNote


data class ReplyNotification(
    override val id: Id,

    override val createdAt: Date,
    override val userId: User.Id,
    override val noteId: Note.Id,
    override val isRead: Boolean

) : Notification(), HasNote

data class RenoteNotification(
    override val id: Id,

    override val createdAt: Date,
    override val userId: User.Id,
    override val noteId: Note.Id,
    override val isRead: Boolean

) : Notification(), HasNote

data class QuoteNotification(
    override val id: Id,

    override val createdAt: Date,
    override val userId: User.Id,
    override val noteId: Note.Id,
    override val isRead: Boolean

) : Notification(), HasNote

data class ReactionNotification(
    override val id: Id,

    override val createdAt: Date,
    override val userId: User.Id,
    override val noteId: Note.Id,
    val reaction: String,
    override val isRead: Boolean

) : Notification(), HasNote

data class PollVoteNotification(
    override val id: Id,
    override val noteId: Note.Id,

    override val createdAt: Date,
    override val userId: User.Id,
    val choice: Int,
    override val isRead: Boolean

) : Notification(), HasNote