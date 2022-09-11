package net.pantasystem.milktea.model.messaging

import kotlinx.coroutines.flow.Flow

/**
 * NOTE: UseCase層として分離したほうがいいのではないか？
 */
interface UnReadMessages {

    fun findAll(): Flow<List<Message>>

    fun findByMessagingId(messagingId: MessagingId): Flow<List<Message>>

    fun findByAccountId(accountId: Long): Flow<List<Message>>
}