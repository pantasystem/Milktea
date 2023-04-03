package net.pantasystem.milktea.data.converters

import net.pantasystem.milktea.api.misskey.notification.NotificationDTO
import net.pantasystem.milktea.data.infrastructure.toGroup
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.group.InvitationId
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notification.*
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationDTOEntityConverter @Inject constructor(
    private val noteDTOEntityConverter: NoteDTOEntityConverter
) {
    suspend fun convert(
        notificationDTO: NotificationDTO,
        account: Account,
    ): Notification {
        val id = Notification.Id(account.accountId, notificationDTO.id)
        return when (notificationDTO.type) {
            "follow" -> {
                FollowNotification(
                    id,
                    notificationDTO.createdAt,
                    User.Id(account.accountId, notificationDTO.userId!!),
                    notificationDTO.isRead ?: true
                )
            }
            "followRequestAccepted" -> {
                FollowRequestAcceptedNotification(
                    id,
                    notificationDTO.createdAt,
                    User.Id(account.accountId, notificationDTO.userId!!),
                    notificationDTO.isRead ?: true
                )
            }
            "receiveFollowRequest" -> {
                ReceiveFollowRequestNotification(
                    id,
                    notificationDTO.createdAt,
                    User.Id(account.accountId, notificationDTO.userId!!),
                    notificationDTO.isRead ?: true
                )
            }
            "mention" -> {
                MentionNotification(
                    id,
                    notificationDTO.createdAt,
                    User.Id(account.accountId, notificationDTO.userId!!),
                    Note.Id(
                        account.accountId,
                        notificationDTO.note?.id ?: throw IllegalStateException("noteId参照不能")
                    ),
                    notificationDTO.isRead ?: true
                )
            }
            "reply" -> {
                ReplyNotification(
                    id,
                    notificationDTO.createdAt,
                    User.Id(account.accountId, notificationDTO.userId!!),
                    Note.Id(
                        account.accountId,
                        notificationDTO.note?.id ?: throw IllegalStateException("noteId参照不能")
                    ),
                    notificationDTO.isRead ?: true
                )
            }
            "renote" -> {
                RenoteNotification(
                    id,
                    notificationDTO.createdAt,
                    User.Id(account.accountId, notificationDTO.userId!!),
                    Note.Id(
                        account.accountId,
                        notificationDTO.note?.id ?: throw IllegalStateException("noteId参照不能")
                    ),
                    notificationDTO.isRead ?: true
                )
            }
            "quote" -> {
                QuoteNotification(
                    id,
                    notificationDTO.createdAt,
                    User.Id(account.accountId, notificationDTO.userId!!),
                    Note.Id(
                        account.accountId,
                        notificationDTO.note?.id ?: throw IllegalStateException("noteId参照不能")
                    ),
                    notificationDTO.isRead ?: true
                )
            }
            "reaction" -> {

                require(notificationDTO.reaction != null) {
                    "想定しないデータ=$notificationDTO"
                }
                require(notificationDTO.note != null)
                val n = noteDTOEntityConverter.convert(notificationDTO.note!!, account)
                ReactionNotification(
                    id,
                    notificationDTO.createdAt,
                    User.Id(account.accountId, notificationDTO.userId!!),
                    n.id,
                    notificationDTO.reaction!!,
                    notificationDTO.isRead ?: true
                )
            }
            "pollVote" -> {
                require(notificationDTO.noteId != null || notificationDTO.note != null)
                require(notificationDTO.choice != null)
                PollVoteNotification(
                    id,
                    Note.Id(
                        account.accountId,
                        notificationDTO.noteId ?: notificationDTO.note?.id!!
                    ),
                    notificationDTO.createdAt,
                    User.Id(account.accountId, notificationDTO.userId!!),
                    notificationDTO.choice!!,
                    notificationDTO.isRead ?: true
                )
            }
            "pollEnded", "poll_finished" -> {
                require(notificationDTO.note != null)
                PollEndedNotification(
                    id,
                    notificationDTO.createdAt,
                    isRead = notificationDTO.isRead ?: true,
                    Note.Id(account.accountId, notificationDTO.note!!.id)
                )
            }
            "groupInvited" -> {
                require(notificationDTO.invitation != null)
                require(notificationDTO.userId != null)
                GroupInvitedNotification(
                    id,
                    isRead = notificationDTO.isRead ?: true,
                    notificationDTO.createdAt,
                    group = notificationDTO.invitation!!.group.toGroup(account.accountId),
                    User.Id(account.accountId, notificationDTO.userId!!),
                    InvitationId(account.accountId, notificationDTO.invitation!!.id),
                )
            }
            else -> {
                return UnknownNotification(
                    id,
                    notificationDTO.createdAt,
                    notificationDTO.isRead ?: false,
                    User.Id(account.accountId, notificationDTO.userId!!),
                    notificationDTO.type
                )
            }
        }
    }
}