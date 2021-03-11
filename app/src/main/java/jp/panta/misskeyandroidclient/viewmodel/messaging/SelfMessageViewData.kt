package jp.panta.misskeyandroidclient.viewmodel.messaging

import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.api.messaging.MessageDTO
import java.lang.IllegalArgumentException

class SelfMessageViewData(message: MessageDTO, account: Account) : MessageViewData(message, account){
    override val avatarIcon: String = message.user?.avatarUrl?: throw IllegalArgumentException("not self message")

    override val name: String = message.user?.name?: message.user?.userName?: throw IllegalArgumentException("not self message")

}