package net.pantasystem.milktea.data.infrastructure.note.timeline

import net.pantasystem.milktea.model.note.timeline.TimelineResponse

interface TimelineLocalDataSource {

    suspend fun getFromCache(
        accountId: Long,
        pageId: Long,
        untilId: String?,
        sinceId: String?,
        limit: Int
    ): Result<TimelineResponse>

    suspend fun saveToCache(
        accountId: Long,
        pageId: Long,
        timelineItems: List<String>,
    ): Result<Unit>
}