package jp.panta.misskeyandroidclient.viewmodel.messaging

import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.messaging.Message

class HistoryViewData (connectionInstance: ConnectionInstance, val message: Message){
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

}