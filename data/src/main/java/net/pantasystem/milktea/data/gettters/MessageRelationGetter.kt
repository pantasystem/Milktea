package net.pantasystem.milktea.data.gettters

import net.pantasystem.milktea.api.misskey.messaging.MessageDTO
import net.pantasystem.milktea.data.infrastructure.entities
import net.pantasystem.milktea.data.infrastructure.messaging.impl.MessageDataSource
import net.pantasystem.milktea.data.infrastructure.toGroup
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.group.GroupDataSource
import net.pantasystem.milktea.model.messaging.Message
import net.pantasystem.milktea.model.messaging.MessageNotFoundException
import net.pantasystem.milktea.model.messaging.MessageRelation
import net.pantasystem.milktea.model.user.UserDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRelationGetter @Inject constructor(
    private val messageDataSource: MessageDataSource,
    private val userDataSource: UserDataSource,
    private val groupDataSource: GroupDataSource,
    private val accountRepository: AccountRepository,
) {

    suspend fun get(account: Account, messageDTO: MessageDTO): MessageRelation {
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
                    accountRepository.get(message.id.accountId)
                )
            }
            is Message.Group -> {
                MessageRelation.Group(
                    message,
                    userDataSource.get(message.userId),
                    accountRepository.get(message.id.accountId)
                )
            }
        }
    }
}