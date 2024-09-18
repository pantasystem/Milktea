package net.pantasystem.milktea.data.infrastructure.note.timeline

import net.pantasystem.milktea.model.note.timeline.TimelineResponse
import net.pantasystem.milktea.model.note.timeline.TimelineType

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

    suspend fun clear(type: TimelineType): Result<Unit>

    suspend fun findFirstLaterId(type: TimelineType): Result<String?>

    suspend fun findLastPreviousId(type: TimelineType): Result<String?>
}