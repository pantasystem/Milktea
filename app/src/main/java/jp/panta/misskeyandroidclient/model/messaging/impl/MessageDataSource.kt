package jp.panta.misskeyandroidclient.model.messaging.impl

import jp.panta.misskeyandroidclient.model.AddResult
import jp.panta.misskeyandroidclient.model.messaging.Message

interface MessageDataSource {
    suspend fun add(message: Message): AddResult

    suspend fun addAll(messages: List<Message>): List<AddResult>

    suspend fun delete(messageId: Message.Id): Boolean

    suspend fun find(messageId: Message.Id): Message?
}

class InMemoryMessageDataSource : MessageDataSource {

    private val messageIdAndMessage = mutableMapOf<Message.Id, Message>()

    override suspend fun add(message: Message): AddResult {
        return createOrUpdate(message)
    }

    override suspend fun addAll(messages: List<Message>): List<AddResult> {
        return messages.map {
            add(it)
        }
    }

    override suspend fun delete(messageId: Message.Id): Boolean {
        return remove(messageId)
    }

    override suspend fun find(messageId: Message.Id): Message? {
        return get(messageId)
    }

    private fun createOrUpdate(message: Message): AddResult {
        synchronized(messageIdAndMessage) {
            messageIdAndMessage[message.id]?.let{
                messageIdAndMessage[message.id] = message
                return AddResult.UPDATED
            }
            messageIdAndMessage[message.id] = message
            return AddResult.CREATED
        }
    }

    private fun get(messageId: Message.Id): Message? {
        synchronized(messageIdAndMessage) {
            return messageIdAndMessage[messageId]
        }
    }

    private fun remove(messageId: Message.Id): Boolean {
        synchronized(messageIdAndMessage) {
            return messageIdAndMessage.remove(messageId) != null
        }
    }

}