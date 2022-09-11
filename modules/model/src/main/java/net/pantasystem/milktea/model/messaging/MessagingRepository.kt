package net.pantasystem.milktea.model.messaging

interface MessagingRepository {
    suspend fun findMessageSummaries(accountId: Long, isGroup: Boolean = false): Result<List<MessageRelation>>
}