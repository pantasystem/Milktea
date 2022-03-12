package jp.panta.misskeyandroidclient.api.misskey.notification

import jp.panta.misskeyandroidclient.api.misskey.notes.NoteDTO
import jp.panta.misskeyandroidclient.api.misskey.notes.toNote
import jp.panta.misskeyandroidclient.api.misskey.users.UserDTO
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notification.*
import jp.panta.misskeyandroidclient.model.users.User
import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.serialization.Serializable
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