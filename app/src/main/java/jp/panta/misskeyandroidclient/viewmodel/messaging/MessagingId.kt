package jp.panta.misskeyandroidclient.viewmodel.messaging

import jp.panta.misskeyandroidclient.model.core.Account
import jp.panta.misskeyandroidclient.model.messaging.Message

class MessagingId(val message: Message, val account: Account){

    val isGroup = message.isGroup()
    val msgId = if(isGroup){
        message.groupId
    }else{
        message.opponentUser(account)?.id
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MessagingId

        if (isGroup != other.isGroup) return false
        if (msgId != other.msgId) return false
        if(account != other.account) return false

        return true
    }

    override fun hashCode(): Int {
        var result = account.hashCode()
        result = 31 * result + isGroup.hashCode()
        result = 31 * result + (msgId?.hashCode() ?: 0)
        return result
    }


}