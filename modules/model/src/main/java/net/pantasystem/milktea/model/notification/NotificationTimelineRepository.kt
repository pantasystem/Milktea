package net.pantasystem.milktea.model.notification

interface NotificationTimelineRepository {

    suspend fun findPreviousTimeline(
        accountId: Long,
        untilId: String? = null,
        limit: Int = 10,
        excludeTypes: List<String>? = null,
        includeTypes: List<String>? = null
    ): Result<List<Notification>>

    suspend fun findLaterTimeline(
        accountId: Long,
        sinceId: String? = null,
        limit: Int = 10,
        excludeTypes: List<String>? = null,
        includeTypes: List<String>? = null,
    ): Result<List<Notification>>
}