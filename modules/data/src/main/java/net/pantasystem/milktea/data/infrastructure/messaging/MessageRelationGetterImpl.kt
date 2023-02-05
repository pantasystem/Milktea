package net.pantasystem.milktea.data.infrastructure.messaging

import net.pantasystem.milktea.api.misskey.messaging.MessageDTO
import net.pantasystem.milktea.data.converters.FilePropertyDTOEntityConverter
import net.pantasystem.milktea.data.converters.UserDTOEntityConverter
import net.pantasystem.milktea.data.infrastructure.toGroup
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.group.Group
import net.pantasystem.milktea.model.group.GroupDataSource
import net.pantasystem.milktea.model.messaging.Message
import net.pantasystem.milktea.model.messaging.MessageNotFoundException
import net.pantasystem.milktea.model.messaging.MessageRelation
import net.pantasystem.milktea.model.messaging.MessageRelationGetter
import net.pantasystem.milktea.model.user.User
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
    private val userDTOEntityConverter: UserDTOEntityConverter,
    private val filePropertyDTOEntityConverter: FilePropertyDTOEntityConverter,
) : MessageRelationGetter, MessageAdder {

    override suspend fun add(account: Account, messageDTO: MessageDTO): MessageRelation {
        val (message, users) = messageDTO.entities(
            account,
            userDTOEntityConverter,
            filePropertyDTOEntityConverter
        )
        messageDataSource.add(message)
        userDataSource.addAll(users)
        messageDTO.group?.let {
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

        return when (message) {
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


suspend fun MessageDTO.entities(
    account: Account,
    userDTOEntityConverter: UserDTOEntityConverter,
    filePropertyDTOEntityConverter: FilePropertyDTOEntityConverter,
): Pair<Message, List<User>> {
    val list = mutableListOf<User>()
    val id = Message.Id(account.accountId, id)
    list.add(userDTOEntityConverter.convert(account, user))
    val message = if (groupId == null) {
        require(recipientId != null)
        Message.Direct(
            id,
            createdAt,
            text,
            User.Id(account.accountId, userId),
            fileId,
            file?.let {
                filePropertyDTOEntityConverter.convert(it, account)
            },
            isRead,
            emojis ?: emptyList(),
            recipientId = User.Id(account.accountId, recipientId!!)
        )
    } else {
        Message.Group(
            id,
            createdAt,
            text,
            User.Id(account.accountId, userId),
            fileId,
            file?.let {
                filePropertyDTOEntityConverter.convert(it, account)
            },
            isRead = reads?.contains(account.remoteId) ?: false,
            emojis ?: emptyList(),
            Group.Id(account.accountId, groupId!!),
            reads = reads?.map {
                User.Id(account.accountId, it)
            } ?: emptyList()
        )
    }
    return message to list
}
