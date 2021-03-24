package jp.panta.misskeyandroidclient.gettters

import jp.panta.misskeyandroidclient.api.messaging.MessageDTO
import jp.panta.misskeyandroidclient.api.messaging.entities
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.messaging.Message
import jp.panta.misskeyandroidclient.model.messaging.MessageNotFoundException
import jp.panta.misskeyandroidclient.model.messaging.MessageRelation
import jp.panta.misskeyandroidclient.model.messaging.impl.MessageDataSource
import jp.panta.misskeyandroidclient.model.users.UserDataSource
import kotlin.jvm.Throws

class MessageRelationGetter(
    private val messageDataSource: MessageDataSource,
    private val userDataSource: UserDataSource,
) {

    suspend fun get(account: Account, messageDTO: MessageDTO): MessageRelation {
        val (message, users) = messageDTO.entities(account)
        messageDataSource.add(message)
        userDataSource.addAll(users)
        return get(message)
    }

    @Throws(MessageNotFoundException::class)
    suspend fun get(messageId: Message.Id): MessageRelation {
        val message = messageDataSource.find(messageId)
            ?: throw MessageNotFoundException(messageId)
        return get(message)
    }

    suspend fun get(message: Message): MessageRelation {
        return when(message) {
            is Message.Direct -> {
                MessageRelation.Direct(
                    message,
                    userDataSource.get(message.userId),
                    userDataSource.get(message.recipientId)
                )
            }
            is Message.Group -> {
                MessageRelation.Group(
                    message,
                    message.group,
                    userDataSource.get(message.userId),
                )
            }
        }
    }
}