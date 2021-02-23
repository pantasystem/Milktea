package jp.panta.misskeyandroidclient.model.notification

import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.api.users.UserDTO
import jp.panta.misskeyandroidclient.serializations.DateSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*



@Serializable
sealed class Notification {
    abstract val id: String
    abstract val userId: String
    abstract val user: UserDTO
    abstract val createdAt: Date
    abstract val isRead: Boolean
}

interface HasNote {
    val note: NoteDTO
}

@SerialName("follow")
@Serializable
data class FollowNotification(
    override val id: String,
    override val user: UserDTO,

    @kotlinx.serialization.Serializable(with = DateSerializer::class)
    override val createdAt: Date,
    override val userId: String,
    override val isRead: Boolean
) : Notification()

@SerialName("followRequestAccepted")
@Serializable
data class FollowRequestAcceptedNotification(
    override val id: String,
    override val user: UserDTO,

    @kotlinx.serialization.Serializable(with = DateSerializer::class)
    override val createdAt: Date,
    override val userId: String,
    override val isRead: Boolean

) : Notification()

@SerialName("receiveFollowRequest")
@Serializable
data class ReceiveFollowRequestNotification(
    override val id: String,
    override val user: UserDTO,

    @kotlinx.serialization.Serializable(with = DateSerializer::class)
    override val createdAt: Date,
    override val userId: String,
    override val isRead: Boolean

) : Notification()

@Serializable
@SerialName("mention")
data class MentionNotification(
    override val id: String,
    override val user: UserDTO,

    @kotlinx.serialization.Serializable(with = DateSerializer::class)
    override val createdAt: Date,
    override val userId: String,
    override val note: NoteDTO,
    override val isRead: Boolean

) : Notification(), HasNote


@Serializable
@SerialName("reply")
data class ReplyNotification(
    override val id: String,
    override val user: UserDTO,

    @kotlinx.serialization.Serializable(with = DateSerializer::class)
    override val createdAt: Date,
    override val userId: String,
    override val note: NoteDTO,
    override val isRead: Boolean

) : Notification(), HasNote

@Serializable
@SerialName("renote")
data class RenoteNotification(
    override val id: String,
    override val user: UserDTO,

    @kotlinx.serialization.Serializable(with = DateSerializer::class)
    override val createdAt: Date,
    override val userId: String,
    override val note: NoteDTO,
    override val isRead: Boolean

) : Notification(), HasNote

@SerialName("quote")
@Serializable
data class QuoteNotification(
    override val id: String,
    override val user: UserDTO,

    @kotlinx.serialization.Serializable(with = DateSerializer::class)
    override val createdAt: Date,
    override val userId: String,
    override val note: NoteDTO,
    override val isRead: Boolean

) : Notification(), HasNote

@SerialName("reaction")
@Serializable
data class ReactionNotification(
    override val id: String,
    override val user: UserDTO,

    @kotlinx.serialization.Serializable(with = DateSerializer::class)
    override val createdAt: Date,
    override val userId: String,
    override val note: NoteDTO,
    val reaction: String,
    override val isRead: Boolean

) : Notification(), HasNote

@SerialName("pollVote")
@Serializable
data class PollVoteNotification(
    override val id: String,
    override val user: UserDTO,

    @kotlinx.serialization.Serializable(with = DateSerializer::class)
    override val createdAt: Date,
    override val userId: String,
    override val note: NoteDTO,
    val choice: Int,
    override val isRead: Boolean

) : Notification(), HasNote