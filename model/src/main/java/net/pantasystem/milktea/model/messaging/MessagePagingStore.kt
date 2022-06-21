package net.pantasystem.milktea.model.messaging

import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.common.PageableState

data class MessagingPagingState(
    val pageState: PageableState<List<Message.Id>>,
)

interface MessagePagingStore {

    val state: Flow<MessagingPagingState>

    suspend fun loadPrevious()
    suspend fun loadFuture()
    suspend fun clear()
    suspend fun setMessagingId(messagingId: MessagingId)
    suspend fun collectReceivedMessageQueue(): Nothing

    fun latestReceivedMessageId(): Message.Id?

    fun onReceiveMessage(msg: Message.Id)
}