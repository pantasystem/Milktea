package jp.panta.misskeyandroidclient.viewmodel.messaging

import jp.panta.misskeyandroidclient.model.messaging.Message
import java.lang.IllegalArgumentException

class RecipientMessageViewData(message: Message) : MessageViewData(message){
    override val avatarIcon: String = message.user?.avatarUrl?: throw IllegalArgumentException("not recipient")
    override val name: String = message.user?.name?: message.recipient?.userName?: throw IllegalArgumentException("not recipient")
}