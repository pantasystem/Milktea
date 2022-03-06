package jp.panta.misskeyandroidclient.gettters

import jp.panta.misskeyandroidclient.api.misskey.groups.toGroup
import jp.panta.misskeyandroidclient.api.misskey.messaging.MessageDTO
import jp.panta.misskeyandroidclient.api.misskey.messaging.entities
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.group.GroupDataSource
import jp.panta.misskeyandroidclient.model.messaging.Message
import jp.panta.misskeyandroidclient.model.messaging.MessageNotFoundException
import jp.panta.misskeyandroidclient.model.messaging.MessageRelation
import jp.panta.misskeyandroidclient.model.messaging.impl.MessageDataSource
import jp.panta.misskeyandroidclient.model.users.UserDataSource
import kotlin.jvm.Throws

class MessageRelationGetter(
    private val messageDataSource: MessageDataSource,
    private val userDataSource: UserDataSource,
    private val groupDataSource: GroupDataSource
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