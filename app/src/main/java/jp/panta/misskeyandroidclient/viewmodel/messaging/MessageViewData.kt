package jp.panta.misskeyandroidclient.viewmodel.messaging

import jp.panta.misskeyandroidclient.model.messaging.Message


abstract class MessageViewData (val message: Message){
    val id = message.id
    abstract val name: String
    abstract val avatarIcon: String
    val text = message.text
    val file = message.file
    val isRead = message.isRead

}