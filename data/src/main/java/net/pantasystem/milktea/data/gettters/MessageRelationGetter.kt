package net.pantasystem.milktea.data.gettters

import net.pantasystem.milktea.api.misskey.messaging.MessageDTO
import net.pantasystem.milktea.data.model.entities
import net.pantasystem.milktea.model.group.GroupDataSource
import net.pantasystem.milktea.model.messaging.Message
import net.pantasystem.milktea.model.messaging.MessageNotFoundException
import net.pantasystem.milktea.model.messaging.MessageRelation
import net.pantasystem.milktea.data.model.messaging.impl.MessageDataSource
import net.pantasystem.milktea.data.model.toGroup
import kotlin.jvm.Throws

class MessageRelationGetter(
    private val messageDataSource: MessageDataSource,
    private val userDataSource: net.pantasystem.milktea.model.user.UserDataSource,
    private val groupDataSource: GroupDataSource
) {

    suspend fun get(account: net.pantasystem.milktea.model.account.Account, messageDTO: MessageDTO): MessageRelation {
        val (message, users) = messageDTO.entities(account)
        messageDataSource.add(message)
        userDataSource.addAll(users)
        messageDTO.group?.let{
            groupDataSource.add(it.toGroup(account.accountId))
        }
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
                )
            }
            is Message.Group -> {
                MessageRelation.Group(
                    message,
                    userDataSource.get(message.userId),
                )
            }
        }
    }
}