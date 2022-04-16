package net.pantasystem.milktea.data.api.misskey.notification

import net.pantasystem.milktea.data.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.data.api.misskey.notes.toNote
import net.pantasystem.milktea.data.api.misskey.users.UserDTO
import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.serialization.Serializable
import net.pantasystem.milktea.data.model.account.Account
import net.pantasystem.milktea.data.model.notes.Note
import net.pantasystem.milktea.data.model.notification.*
import net.pantasystem.milktea.data.model.users.User
import java.lang.IllegalStateException

@Serializable
data class NotificationDTO(
    val id: String,
    @kotlinx.serialization.Serializable(with = InstantIso8601Serializer::class) val createdAt: Instant,
    val type: String,
    val userId: String,
    val user: UserDTO?,
    val note: NoteDTO? = null,
    val noteId: String? = null,
    val reaction: String? = null,
    val isRead: Boolean?,
    val choice: Int? = null
) {

    fun toNotification(account: Account): Notification {
        val id = Notification.Id(account.accountId, this.id)
        return when (this.type) {
            "follow" -> {
                FollowNotification(
                    id, createdAt, User.Id(account.accountId, this.userId), isRead ?: true
                )
            }
            "followRequestAccepted" -> {
                FollowRequestAcceptedNotification(
                    id, createdAt, User.Id(account.accountId, this.userId), isRead ?: true
                )
            }
            "receiveFollowRequest" -> {
                ReceiveFollowRequestNotification(
                    id, createdAt, User.Id(account.accountId, this.userId), isRead ?: true
                )
            }
            "mention" -> {
                MentionNotification(
                    id,
                    createdAt,
                    User.Id(account.accountId, this.userId),
                    Note.Id(
                        account.accountId,
                        note?.id ?: throw IllegalStateException("noteId参照不能")
                    ),
                    isRead ?: true
                )
            }
            "reply" -> {
                ReplyNotification(
                    id,
                    createdAt,
                    User.Id(account.accountId, this.userId),
                    Note.Id(
                        account.accountId,
                        note?.id ?: throw IllegalStateException("noteId参照不能")
                    ),
                    isRead ?: true
                )
            }
            "renote" -> {
                RenoteNotification(
                    id,
                    createdAt,
                    User.Id(account.accountId, this.userId),
                    Note.Id(
                        account.accountId,
                        note?.id ?: throw IllegalStateException("noteId参照不能")
                    ),
                    isRead ?: true
                )
            }
            "quote" -> {
                QuoteNotification(
                    id,
                    createdAt,
                    User.Id(account.accountId, this.userId),
                    Note.Id(
                        account.accountId,
                        note?.id ?: throw IllegalStateException("noteId参照不能")
                    ),
                    isRead ?: true
                )
            }
            "reaction" -> {

                require(reaction != null) {
                    "想定しないデータ=$this"
                }
                require(note != null)
                val n = note.toNote(account)
                ReactionNotification(
                    id,
                    createdAt,
                    User.Id(account.accountId, this.userId),
                    n.id,
                    reaction,
                    isRead ?: true
                )
            }
            "pollVote" -> {
                require(noteId != null || note != null)
                require(choice != null)
                PollVoteNotification(
                    id,
                    Note.Id(account.accountId, noteId ?: note?.id!!),
                    createdAt,
                    User.Id(account.accountId, this.userId),
                    choice,
                    isRead ?: true
                )
            }
            "pollEnded" -> {
                require(note != null)
                PollEndedNotification(
                    id,
                    createdAt,
                    isRead = isRead ?: true,
                    Note.Id(account.accountId, note.id)
                )
            }
            else -> {
                return UnknownNotification(
                    id,
                    createdAt,
                    isRead ?: false,
                    User.Id(account.accountId, this.userId),
                    this.type
                )
            }
        }
    }
}