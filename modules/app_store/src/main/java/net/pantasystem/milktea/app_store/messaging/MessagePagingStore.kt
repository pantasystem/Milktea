package net.pantasystem.milktea.app_store.messaging

import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.model.messaging.Message
import net.pantasystem.milktea.model.messaging.MessagingId

data class MessagingPagingState(
    val pageState: PageableState<List<Message.Id>>,
)

interface MessagePagingStore {

    val state: Flow<MessagingPagingState>

    suspend fun loadPrevious()
    suspend fun clear()
    suspend fun setMessagingId(messagingId: MessagingId)
    suspend fun collectReceivedMessageQueue(): Nothing

    fun latestReceivedMessageId(): Message.Id?

    fun onReceiveMessage(msg: Message.Id)
}