package jp.panta.misskeyandroidclient.model.messaging

import jp.panta.misskeyandroidclient.api.messaging.MessageDTO
import jp.panta.misskeyandroidclient.model.account.Account
import java.io.Serializable

class MessagingId(val message: MessageDTO, val account: Account) : Serializable{

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
        if(account.accountId != other.account.accountId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = account.hashCode()
        result = 31 * result + isGroup.hashCode()
        result = 31 * result + (msgId?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "MessagingId(msgId=$msgId, account=${account.accountId})"
    }


}