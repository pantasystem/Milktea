package net.pantasystem.milktea.notification.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import net.pantasystem.milktea.common_android.resource.StringSource
import net.pantasystem.milktea.model.notification.*
import net.pantasystem.milktea.model.setting.DefaultConfig
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData
import net.pantasystem.milktea.notification.R

class NotificationViewData(
    val notification: NotificationRelation,
    val noteViewData: PlaneNoteViewData?,
    configRepository: LocalConfigRepository,
    coroutineScope: CoroutineScope,
) {
    enum class Type(val default: String) {
        FOLLOW("follow"),
        MENTION("mention"),
        REPLY("reply"),
        RENOTE("renote"),
        QUOTE("quote"),
        REACTION("reaction"),
        POLL_VOTE("pollVote"),
        RECEIVE_FOLLOW_REQUEST("receiveFollowRequest"),
        FOLLOW_REQUEST_ACCEPTED("followRequestAccepted"),
        POLL_ENDED("pollEnded"),
        UNKNOWN("unknown"),
        GROUP_INVITED("groupInvited"),
        STATUS("status"),
        FAVORITE("favorite"),
    }

    val id = notification.notification.id

    val type: Type = when (notification.notification) {
        is FollowNotification -> Type.FOLLOW
        is MentionNotification -> Type.MENTION
        is ReplyNotification -> Type.REPLY
        is RenoteNotification -> Type.RENOTE
        is QuoteNotification -> Type.QUOTE
        is ReactionNotification -> Type.REACTION
        is PollVoteNotification -> Type.POLL_VOTE
        is ReceiveFollowRequestNotification -> Type.RECEIVE_FOLLOW_REQUEST
        is FollowRequestAcceptedNotification -> Type.FOLLOW_REQUEST_ACCEPTED
        is PollEndedNotification -> Type.POLL_ENDED
        is GroupInvitedNotification -> Type.GROUP_INVITED
        is UnknownNotification -> Type.UNKNOWN
        is FavoriteNotification -> Type.FAVORITE
        is PostNotification -> Type.STATUS
    }
    val statusType: String = type.default

    val user: User? = notification.user
    val avatarIconUrl = notification.user?.avatarUrl
    val name = notification.user?.name
    val userName = notification.user?.userName

    val reaction =
        (notification.notification as? ReactionNotification)?.reaction

    val groupInvitedMessageSource: StringSource? = (notification.notification as? GroupInvitedNotification?)?.let {
        StringSource(R.string.notification_group_invited_message, it.group.name)
    }

    val followRequestMessageSource: StringSource? = (notification.notification as? ReceiveFollowRequestNotification?)?.let {
        StringSource(R.string.follow_requested_by, name ?: "")
    }

    val config = configRepository.observe().stateIn(coroutineScope, SharingStarted.WhileSubscribed(5_000), DefaultConfig.config)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NotificationViewData

        if (notification != other.notification) return false
        if (id != other.id) return false
        if (noteViewData != other.noteViewData) return false
        if (statusType != other.statusType) return false
        if (avatarIconUrl != other.avatarIconUrl) return false
        if (name != other.name) return false
        if (userName != other.userName) return false
        if (reaction != other.reaction) return false

        return true
    }

    override fun hashCode(): Int {
        var result = notification.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + (noteViewData?.hashCode() ?: 0)
        result = 31 * result + statusType.hashCode()
        result = 31 * result + (avatarIconUrl?.hashCode() ?: 0)
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + userName.hashCode()
        result = 31 * result + (reaction?.hashCode() ?: 0)
        return result
    }
}