package jp.panta.misskeyandroidclient.model.messaging

import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.Account
import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable

@Serializable
data class MessageAction(
    val i: String?,
    val userId: String?,
    val groupId: String?,
    val text: String?,
    val fileId: String?,
    val messageId: String?
): JavaSerializable{
    class Factory(val account: Account, val message: Message){
        fun actionCreateMessage(text: String?, fileId: String?, encryption: Encryption): MessageAction{
            return MessageAction(
                account.getI(encryption),
                if(message.isGroup()) null else message.opponentUser(account)?.id,
                message.group?.id,
                text,
                fileId,
            null
            )
        }

        fun actionDeleteMessage(message: Message, encryption: Encryption): MessageAction{
            return MessageAction(
                account.getI(encryption),
                null,
                null,
                null,
                null,
                message.id
            )
        }

        fun actionRead(message: Message, encryption: Encryption): MessageAction{
            return MessageAction(
                account.getI(encryption),
                null,
                null,
                null,
                null,
                message.id
            )
        }
    }
}