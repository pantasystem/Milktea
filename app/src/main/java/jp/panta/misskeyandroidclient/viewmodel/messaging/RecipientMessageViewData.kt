package jp.panta.misskeyandroidclient.viewmodel.messaging

import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.api.messaging.MessageDTO
import java.lang.IllegalArgumentException

class RecipientMessageViewData(message: MessageDTO, account: Account) : MessageViewData(message, account){
    override val avatarIcon: String = message.user?.avatarUrl?: throw IllegalArgumentException("not recipient")
    override val name: String = message.user?.name?: message.recipient?.userName?: ""
}