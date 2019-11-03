package jp.panta.misskeyandroidclient.viewmodel.messaging

import jp.panta.misskeyandroidclient.model.messaging.Message
import java.lang.IllegalArgumentException

class RecipientMessageViewData(message: Message) : MessageViewData(message){
    override val avatarIcon: String = message.recipient?.avatarUrl?: throw IllegalArgumentException("not recipient")
    override val name: String = message.recipient?.name?: message.recipient?.userName?: throw IllegalArgumentException("not recipient")
}