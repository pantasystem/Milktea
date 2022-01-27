package jp.panta.misskeyandroidclient.ui.notification.viewmodel

import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.notes.NoteCaptureAPIAdapter
import jp.panta.misskeyandroidclient.model.notes.NoteTranslationStore
import jp.panta.misskeyandroidclient.model.notification.*
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.DetermineTextLength
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.PlaneNoteViewData

class NotificationViewData(val notification: NotificationRelation, account: Account, noteCaptureAPIAdapter: NoteCaptureAPIAdapter, translationStore: NoteTranslationStore) {
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
        UNKNOWN("unknown"),

    }
    val id = notification.notification.id
    val noteViewData: PlaneNoteViewData? = if(notification.notification is HasNote) PlaneNoteViewData(notification.note!!, account, noteCaptureAPIAdapter, translationStore) else null

    val type: Type = when(notification.notification){
        is FollowNotification -> Type.FOLLOW
        is MentionNotification -> Type.MENTION
        is ReplyNotification -> Type.REPLY
        is RenoteNotification -> Type.RENOTE
        is QuoteNotification -> Type.QUOTE
        is ReactionNotification -> Type.REACTION
        is PollVoteNotification -> Type.POLL_VOTE
        is ReceiveFollowRequestNotification -> Type.RECEIVE_FOLLOW_REQUEST
        is FollowRequestAcceptedNotification -> Type.FOLLOW_REQUEST_ACCEPTED
        is UnknownNotification -> Type.UNKNOWN
    }
    val statusType: String = type.default

    val user: User = notification.user
    val avatarIconUrl = notification.user.avatarUrl
    val name = notification.user.name
    val userName = notification.user.userName

    val reaction =  (notification.notification as? ReactionNotification)?.reaction


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