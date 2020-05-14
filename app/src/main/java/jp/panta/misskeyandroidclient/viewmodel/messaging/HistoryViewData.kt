package jp.panta.misskeyandroidclient.viewmodel.messaging

import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.mfm.MFMParser
import jp.panta.misskeyandroidclient.model.core.Account
import jp.panta.misskeyandroidclient.model.messaging.Message

class HistoryViewData (account: Account, message: Message){
    val messagingId = message.messagingId(account)
    val message = MutableLiveData<Message>(message)
    val id = message.id
    val isGroup = message.group != null
    val group = message.group
    val partner = if(message.recipient?.id == account.id){
        message.user
    }else{
        message.recipient
    }

    val historyIcon = if(isGroup) {
        message.user?.avatarUrl
    }else{
        partner?.avatarUrl
    }


    val title = if(isGroup){
        "${message.group?.name}"
    }else{
        val host = partner?.host
        "@${partner?.userName}" + if(host != null) "@$host" else ""
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HistoryViewData

        if (messagingId != other.messagingId) return false
        if (message != other.message) return false
        if (id != other.id) return false
        if (isGroup != other.isGroup) return false
        if (group != other.group) return false
        if (partner != other.partner) return false
        if (historyIcon != other.historyIcon) return false
        if (title != other.title) return false

        return true
    }

    override fun hashCode(): Int {
        var result = messagingId.hashCode()
        result = 31 * result + message.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + isGroup.hashCode()
        result = 31 * result + (group?.hashCode() ?: 0)
        result = 31 * result + (partner?.hashCode() ?: 0)
        result = 31 * result + (historyIcon?.hashCode() ?: 0)
        result = 31 * result + title.hashCode()
        return result
    }

    override fun toString(): String {
        return "HistoryViewData(messagingId=$messagingId, message=$message, id='$id', isGroup=$isGroup, group=$group, partner=$partner, historyIcon=$historyIcon, title='$title')"
    }


}