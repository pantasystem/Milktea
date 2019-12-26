package jp.panta.misskeyandroidclient.model.messaging

import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import java.io.Serializable

data class MessageAction(
    val i: String?,
    val userId: String?,
    val groupId: String?,
    val text: String?,
    val fileId: String?,
    val messageId: String?
): Serializable{
    class Factory(val connectionInstance: ConnectionInstance, val message: Message){
        fun actionCreateMessage(text: String?, fileId: String?, encryption: Encryption): MessageAction{
            return MessageAction(
                connectionInstance.getI(encryption),
                if(message.isGroup()) null else message.opponentUser(connectionInstance)?.id,
                message.group?.id,
                text,
                fileId,
            null
            )
        }

        fun actionDeleteMessage(message: Message, encryption: Encryption): MessageAction{
            return MessageAction(
                connectionInstance.getI(encryption),
                null,
                null,
                null,
                null,
                message.id
            )
        }

        fun actionRead(message: Message, encryption: Encryption): MessageAction{
            return MessageAction(
                connectionInstance.getI(encryption),
                null,
                null,
                null,
                null,
                message.id
            )
        }
    }
}