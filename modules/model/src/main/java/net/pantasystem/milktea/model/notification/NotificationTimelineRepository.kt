package net.pantasystem.milktea.model.notification

interface NotificationTimelineRepository {

    suspend fun findPreviousTimeline(
        accountId: Long,
        untilId: String? = null,
        limit: Int = 10,
    ): Result<List<Notification>>
}