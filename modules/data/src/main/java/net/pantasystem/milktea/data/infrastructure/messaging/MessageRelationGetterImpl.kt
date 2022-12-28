package net.pantasystem.milktea.data.infrastructure.messaging

import net.pantasystem.milktea.api.misskey.messaging.MessageDTO
import net.pantasystem.milktea.data.infrastructure.entities
import net.pantasystem.milktea.data.infrastructure.toGroup
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.group.GroupDataSource
import net.pantasystem.milktea.model.messaging.Message
import net.pantasystem.milktea.model.messaging.MessageNotFoundException
import net.pantasystem.milktea.model.messaging.MessageRelation
import net.pantasystem.milktea.model.messaging.MessageRelationGetter
import net.pantasystem.milktea.model.user.UserDataSource
import javax.inject.Inject
import javax.inject.Singleton


interface MessageAdder {
    suspend fun add(account: Account, messageDTO: MessageDTO): MessageRelation
}

@Singleton
class MessageRelationGetterImpl @Inject constructor(
    private val messageDataSource: MessageDataSource,
    private val userDataSource: UserDataSource,
    private val groupDataSource: GroupDataSource,
    private val accountRepository: AccountRepository,
) : MessageRelationGetter, MessageAdder {

    override suspend fun add(account: Account, messageDTO: MessageDTO): MessageRelation {
        val (message, users) = messageDTO.entities(account)
        messageDataSource.add(message)
        userDataSource.addAll(users)
        messageDTO.group?.let{
            groupDataSource.add(it.toGroup(account.accountId))
        }
        return get(message)
    }

    @Throws(MessageNotFoundException::class)
    override suspend fun get(messageId: Message.Id): MessageRelation {
        val message = messageDataSource.find(messageId).getOrNull()
            ?: throw MessageNotFoundException(messageId)
        return get(message)
    }

    override suspend fun get(message: Message): MessageRelation {

        return when(message) {
            is Message.Direct -> {
                MessageRelation.Direct(
                    message,
                    userDataSource.get(message.userId).getOrThrow(),
                    accountRepository.get(message.id.accountId).getOrThrow()
                )
            }
            is Message.Group -> {
                MessageRelation.Group(
                    message,
                    userDataSource.get(message.userId).getOrThrow(),
                    accountRepository.get(message.id.accountId).getOrThrow()
                )
            }
        }
    }
}