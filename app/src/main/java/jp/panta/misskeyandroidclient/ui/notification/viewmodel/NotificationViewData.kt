package jp.panta.misskeyandroidclient.ui.notification.viewmodel

import net.pantasystem.milktea.data.model.notes.NoteCaptureAPIAdapter
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.PlaneNoteViewData

class NotificationViewData(val notification: net.pantasystem.milktea.model.notification.NotificationRelation, account: net.pantasystem.milktea.model.account.Account, noteCaptureAPIAdapter: NoteCaptureAPIAdapter, translationStore: net.pantasystem.milktea.model.notes.NoteTranslationStore) {
    enum class Type(val default: String){
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

    }
    val id = notification.notification.id
    val noteViewData: PlaneNoteViewData? = if(notification.notification is net.pantasystem.milktea.model.notification.HasNote) PlaneNoteViewData(notification.note!!, account, noteCaptureAPIAdapter, translationStore) else null

    val type: Type = when(notification.notification){
        is net.pantasystem.milktea.model.notification.FollowNotification -> Type.FOLLOW
        is net.pantasystem.milktea.model.notification.MentionNotification -> Type.MENTION
        is net.pantasystem.milktea.model.notification.ReplyNotification -> Type.REPLY
        is net.pantasystem.milktea.model.notification.RenoteNotification -> Type.RENOTE
        is net.pantasystem.milktea.model.notification.QuoteNotification -> Type.QUOTE
        is net.pantasystem.milktea.model.notification.ReactionNotification -> Type.REACTION
        is net.pantasystem.milktea.model.notification.PollVoteNotification -> Type.POLL_VOTE
        is net.pantasystem.milktea.model.notification.ReceiveFollowRequestNotification -> Type.RECEIVE_FOLLOW_REQUEST
        is net.pantasystem.milktea.model.notification.FollowRequestAcceptedNotification -> Type.FOLLOW_REQUEST_ACCEPTED
        is net.pantasystem.milktea.model.notification.PollEndedNotification -> Type.POLL_ENDED
        is net.pantasystem.milktea.model.notification.UnknownNotification -> Type.UNKNOWN
    }
    val statusType: String = type.default

    val user: net.pantasystem.milktea.model.user.User? = notification.user
    val avatarIconUrl = notification.user?.avatarUrl
    val name = notification.user?.name
    val userName = notification.user?.userName

    val reaction =  (notification.notification as? net.pantasystem.milktea.model.notification.ReactionNotification)?.reaction


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