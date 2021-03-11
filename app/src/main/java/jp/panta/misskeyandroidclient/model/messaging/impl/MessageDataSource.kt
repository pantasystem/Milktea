package jp.panta.misskeyandroidclient.model.messaging.impl

import jp.panta.misskeyandroidclient.model.AddResult
import jp.panta.misskeyandroidclient.model.messaging.Message

interface MessageDataSource {
    suspend fun add(message: Message): AddResult

    suspend fun addAll(message: Message): List<AddResult>

    suspend fun delete(messageId: Message.Id): Boolean

    suspend fun find(messageId: Message.Id): Message
}

