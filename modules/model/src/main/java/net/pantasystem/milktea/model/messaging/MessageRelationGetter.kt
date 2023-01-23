package net.pantasystem.milktea.model.messaging

interface MessageRelationGetter {
    suspend fun get(messageId: Message.Id): MessageRelation

    suspend fun get(message: Message): MessageRelation
}
