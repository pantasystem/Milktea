package jp.panta.misskeyandroidclient.model.messaging.impl

import jp.panta.misskeyandroidclient.api.messaging.MessageAction
import jp.panta.misskeyandroidclient.api.messaging.MessageDTO
import jp.panta.misskeyandroidclient.model.messaging.CreateMessage
import jp.panta.misskeyandroidclient.model.messaging.Message
import jp.panta.misskeyandroidclient.model.messaging.MessageRepository
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import java.io.IOException
import java.lang.IllegalStateException
import kotlin.jvm.Throws

@Suppress("BlockingMethodInNonBlockingContext")
class MessageRepositoryImpl(
    val miCore: MiCore
) : MessageRepository {
    private val accountRepository = miCore.getAccountRepository()

    @Throws(IOException::class)
    override suspend fun read(messageId: Message.Id): Boolean {
        val account = accountRepository.get(messageId.accountId)
        val result = miCore.getMisskeyAPI(account).readMessage(
            MessageAction(
                account.getI(miCore.getEncryption()),
                null,
                null,
                null,
                null,
                messageId.messageId
            )
        ).execute().isSuccessful

        if(result) {
            miCore.getMessageDataSource().find(messageId)?.read()?.let{
                miCore.getMessageDataSource().add(it)
            }
        }

        return result
    }

    @Throws(IOException::class)
    override suspend fun create(createMessage: CreateMessage): Message {
        val account = accountRepository.get(createMessage.accountId)
        val i = account.getI(miCore.getEncryption())
        val action = when(createMessage) {
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

        val body: MessageDTO = miCore.getMisskeyAPI(account).createMessage(action).execute().body()
            ?: throw IllegalStateException("メッセージの作成に失敗しました")

        return miCore.getGetters().messageRelationGetter.get(account, body).message

    }

    @Throws(IOException::class)
    override suspend fun delete(messageId: Message.Id): Boolean {
        val account = accountRepository.get(messageId.accountId)
        val result = miCore.getMisskeyAPI(account).deleteMessage(
            MessageAction(
                account.getI(miCore.getEncryption()),
                null,
                null,
                null,
                null,
                messageId.messageId
            )
        ).execute().isSuccessful

        if(result) {
            miCore.getMessageDataSource().delete(messageId)
        }

        return result
    }


}