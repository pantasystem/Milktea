package net.pantasystem.milktea.model.messaging

interface MessageRepository {

    suspend fun read(messageId: Message.Id): Boolean

    suspend fun create(createMessage: CreateMessage): Message

    suspend fun delete(messageId: Message.Id): Boolean

}