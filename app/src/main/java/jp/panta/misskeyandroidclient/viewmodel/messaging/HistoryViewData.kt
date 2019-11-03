package jp.panta.misskeyandroidclient.viewmodel.messaging

import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.messaging.Message

class HistoryViewData (connectionInstance: ConnectionInstance, val message: Message){
    val id = message.id
    val isGroup = message.group != null
    val partner = if(message.recipient?.id == connectionInstance.userId){
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
        "#${message.group?.name}"
    }else{
        val host = partner?.host
        "@${partner?.userName}" + if(host != null) "@$host" else ""
    }

    val text = message.text
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HistoryViewData

        if (message != other.message) return false
        if (isGroup != other.isGroup) return false
        if (partner != other.partner) return false
        if (historyIcon != other.historyIcon) return false
        if (title != other.title) return false
        if (text != other.text) return false

        return true
    }

    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + isGroup.hashCode()
        result = 31 * result + (partner?.hashCode() ?: 0)
        result = 31 * result + (historyIcon?.hashCode() ?: 0)
        result = 31 * result + title.hashCode()
        result = 31 * result + (text?.hashCode() ?: 0)
        return result
    }

}