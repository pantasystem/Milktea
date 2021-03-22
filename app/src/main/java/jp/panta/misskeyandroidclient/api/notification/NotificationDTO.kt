package jp.panta.misskeyandroidclient.api.notification

import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.api.notes.toNote
import jp.panta.misskeyandroidclient.api.users.UserDTO
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notification.*
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.serializations.DateSerializer
import kotlinx.serialization.Serializable
import java.lang.IllegalStateException
import java.util.*

@Serializable
data class NotificationDTO(
    val id: String,
    @kotlinx.serialization.Serializable(with = DateSerializer::class) val createdAt: Date,
    val type: String,
    val userId: String,
    val user: UserDTO,
    val note: NoteDTO? = null,
    val noteId: String? = null,
    val reaction: String? = null,
    val isRead: Boolean,
    val choice: Int? = null
) {

    fun toNotification(account: Account): Notification {
        val id = Notification.Id(account.accountId, this.id)
        return when(this.type) {
            "follow" -> {
                FollowNotification(
                    id, createdAt, User.Id(account.accountId, this.userId), isRead
                )
            }
            "followRequestAccepted" -> {
                FollowRequestAcceptedNotification(
                    id, createdAt, User.Id(account.accountId, this.userId), isRead
                )
            }
            "receiveFollowRequest" -> {
                ReceiveFollowRequestNotification(
                    id, createdAt, User.Id(account.accountId, this.userId), isRead
                )
            }
            "mention" -> {
                MentionNotification(
                    id, createdAt, User.Id(account.accountId, this.userId), Note.Id(account.accountId, noteId?: throw IllegalStateException("noteId参照不能")),isRead
                )
            }
            "reply" -> {
                ReplyNotification(
                    id, createdAt, User.Id(account.accountId, this.userId), Note.Id(account.accountId, noteId?: throw IllegalStateException("noteId参照不能")),isRead
                )
            }
            "renote" -> {
                RenoteNotification(
                    id, createdAt, User.Id(account.accountId, this.userId), Note.Id(account.accountId, noteId?: throw IllegalStateException("noteId参照不能")),isRead
                )
            }
            "quote" -> {
                QuoteNotification(
                    id, createdAt, User.Id(account.accountId, this.userId), Note.Id(account.accountId, noteId?: throw IllegalStateException("noteId参照不能")),isRead
                )
            }
            "reaction" -> {

                require(reaction != null) {
                    "想定しないデータ=$this"
                }
                require(note != null)
                val n = note.toNote(account)
                ReactionNotification(
                    id, createdAt, User.Id(account.accountId, this.userId), n.id, reaction, isRead
                )
            }
            "pollVoted" -> {
                require(noteId != null)
                require(choice != null)
                PollVoteNotification(
                    id, Note.Id(account.accountId, noteId), createdAt, User.Id(account.accountId, this.userId), choice, isRead
                )
            }
            else -> throw IllegalStateException("対応していないタイプの通知です。")
        }
    }
}