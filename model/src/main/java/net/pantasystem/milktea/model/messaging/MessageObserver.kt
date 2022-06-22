package net.pantasystem.milktea.model.messaging

import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.model.account.Account

interface MessageObserver {
    fun observeAllAccountsMessages(): Flow<Message>
    fun observeByMessagingId(messagingId: MessagingId): Flow<Message>
    fun observeAccountMessages(ac: Account): Flow<Message>
}
