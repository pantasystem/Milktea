package net.pantasystem.milktea.model.notification

interface NotificationTimelineRepository {

    suspend fun findPreviousTimeline(
        accountId: Long,
        sinceId: String? = null,
        limit: Int = 10,
    ): Result<List<Notification>>
}