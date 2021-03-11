package jp.panta.misskeyandroidclient.gettters

import jp.panta.misskeyandroidclient.api.messaging.MessageDTO
import jp.panta.misskeyandroidclient.api.messaging.entities
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.messaging.Message
import jp.panta.misskeyandroidclient.model.messaging.MessageRelation
import jp.panta.misskeyandroidclient.model.messaging.impl.MessageDataSource
import jp.panta.misskeyandroidclient.model.users.UserRepository

class MessageRelationGetter(
    private val messageDataSource: MessageDataSource,
    private val userRepository: UserRepository,
) {

    suspend fun get(account: Account, messageDTO: MessageDTO): MessageRelation {
        val (message, users) = messageDTO.entities(account)
        messageDataSource.add(message)
        userRepository.addAll(users)
        return when(message) {
            is Message.Direct -> {
                MessageRelation.Direct(
                    message,
                    userRepository.get(message.userId),
                    userRepository.get(message.recipientId)
                )
            }
            is Message.Group -> {
                MessageRelation.Group(
                    message,
                    message.group,
                    userRepository.get(message.userId),
                )
            }
        }
    }
}