package jp.panta.misskeyandroidclient.ui.messaging.viewmodel

import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.messaging.MessageHistoryRelation
import net.pantasystem.milktea.model.messaging.UnReadMessages
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted

class HistoryViewData (account: net.pantasystem.milktea.model.account.Account, val message: net.pantasystem.milktea.model.messaging.MessageHistoryRelation, unReadMessages: net.pantasystem.milktea.model.messaging.UnReadMessages, coroutineScope: CoroutineScope){
    val messagingId = message.message.messagingId(account)
    val isGroup = message is net.pantasystem.milktea.model.messaging.MessageHistoryRelation.Group
    val group = (message as? net.pantasystem.milktea.model.messaging.MessageHistoryRelation.Group)?.group
    val partner = (message as? net.pantasystem.milktea.model.messaging.MessageHistoryRelation.Direct)?.let {
        if(message.recipient.id.id == account.remoteId){
            message.user
        }else{
            message.recipient
        }
    }

    val historyIcon = if(isGroup) {
        message.user.avatarUrl
    }else{
        partner?.avatarUrl
    }


    val title = if(isGroup){
        "${group?.name}"
    }else{
        partner?.getDisplayUserName()
    }
    private val mUnreadMessages = unReadMessages.findByMessagingId(messagingId)
    val unreadMessages = mUnreadMessages.stateIn(coroutineScope, SharingStarted.Lazily, emptyList())
    val unreadMessageCount = mUnreadMessages.map {
        it.size
    }.stateIn(coroutineScope, SharingStarted.Lazily, 0)


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HistoryViewData

        if (messagingId != other.messagingId) return false
        if (message != other.message) return false
        if (isGroup != other.isGroup) return false
        if (group != other.group) return false
        if (partner != other.partner) return false
        if (historyIcon != other.historyIcon) return false
        if (title != other.title) return false
        if (mUnreadMessages != other.mUnreadMessages) return false
        if (unreadMessages != other.unreadMessages) return false
        if (unreadMessageCount != other.unreadMessageCount) return false

        return true
    }

    override fun hashCode(): Int {
        var result = messagingId.hashCode()
        result = 31 * result + message.hashCode()
        result = 31 * result + isGroup.hashCode()
        result = 31 * result + (group?.hashCode() ?: 0)
        result = 31 * result + (partner?.hashCode() ?: 0)
        result = 31 * result + (historyIcon?.hashCode() ?: 0)
        result = 31 * result + title.hashCode()
        result = 31 * result + mUnreadMessages.hashCode()
        result = 31 * result + unreadMessages.hashCode()
        result = 31 * result + unreadMessageCount.hashCode()
        return result
    }

}