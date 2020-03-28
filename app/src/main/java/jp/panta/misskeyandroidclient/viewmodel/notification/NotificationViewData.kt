package jp.panta.misskeyandroidclient.viewmodel.notification

import jp.panta.misskeyandroidclient.model.core.Account
import jp.panta.misskeyandroidclient.model.notification.Notification
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData
import java.lang.IllegalArgumentException

class NotificationViewData(val notification: Notification, account: Account) {
    enum class Type(val default: String){
        FOLLOW("follow"),
        MENTION("mention"),
        REPLY("reply"),
        RENOTE("renote"),
        QUOTE("quote"),
        REACTION("reaction"),
        POLL_VOTE("pollVote"),
        RECEIVE_FOLLOW_REQUEST("receiveFollowRequest")
    }
    val id = notification.id
    val noteViewData: PlaneNoteViewData? = if(notification.note == null) null else PlaneNoteViewData(notification.note, account)

    val statusType: String = notification.type
    val type: Type? = Type.values().firstOrNull {
        it.default == notification.type
    }

    val user: User = notification.user
    val avatarIconUrl = notification.user.avatarUrl
    val name = notification.user.name
    val userName = notification.user.userName

    val reaction =  notification.reaction
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