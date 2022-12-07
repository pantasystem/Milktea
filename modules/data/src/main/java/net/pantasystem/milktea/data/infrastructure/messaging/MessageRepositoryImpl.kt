package net.pantasystem.milktea.data.infrastructure.messaging

import net.pantasystem.milktea.api.misskey.messaging.MessageAction
import net.pantasystem.milktea.api.misskey.messaging.MessageDTO
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.gettters.MessageAdder
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.messaging.CreateMessage
import net.pantasystem.milktea.model.messaging.Message
import net.pantasystem.milktea.model.messaging.MessageRepository
import java.io.IOException
import javax.inject.Inject

@Suppress("BlockingMethodInNonBlockingContext")
class MessageRepositoryImpl @Inject constructor(
    val misskeyAPIProvider: MisskeyAPIProvider,
    val messageDataSource: MessageDataSource,
    val accountRepository: AccountRepository,
    val messageAdder: MessageAdder,
) : MessageRepository {

    @Throws(IOException::class)
    override suspend fun read(messageId: Message.Id): Boolean {
        val account = accountRepository.get(messageId.accountId).getOrThrow()
        val result = misskeyAPIProvider.get(account).readMessage(
            MessageAction(
                account.token,
                null,
                null,
                null,
                null,
                messageId.messageId
            )
        ).isSuccessful

        if (result) {
            messageDataSource.find(messageId).getOrNull()?.read()?.let {
                messageDataSource.add(it)
            }
        }

        return result
    }

    @Throws(IOException::class)
    override suspend fun create(createMessage: CreateMessage): Message {
        val account = accountRepository.get(createMessage.accountId).getOrThrow()
        val i = account.token
        val action = when (createMessage) {
            is CreateMessage.Group -> {
                MessageAction(
                    i,
                    groupId = createMessage.groupId.groupId,
                    text = createMessage.text,
                    fileId = createMessage.fileId,
                    messageId = null,
                    userId = null
                )
            }
            is CreateMessage.Direct -> {
                MessageAction(
                    i,
                    groupId = null,
                    text = createMessage.text,
                    fileId = createMessage.fileId,
                    messageId = null,
                    userId = createMessage.userId.id
                )
            }
        }

        val body: MessageDTO = misskeyAPIProvider.get(account).createMessage(action)
            .throwIfHasError()
            .body() ?: throw IllegalStateException("メッセージの作成に失敗しました")

        return messageAdder.add(account, body).message

    }

    @Throws(IOException::class)
    override suspend fun delete(messageId: Message.Id): Boolean {
        val account = accountRepository.get(messageId.accountId).getOrThrow()
        val result = misskeyAPIProvider.get(account).deleteMessage(
            MessageAction(
                account.token,
                null,
                null,
                null,
                null,
                messageId.messageId
            )
        ).isSuccessful

        if (result) {
            messageDataSource.delete(messageId)
        }

        return result
    }


}