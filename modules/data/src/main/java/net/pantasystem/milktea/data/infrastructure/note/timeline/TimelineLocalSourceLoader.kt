package net.pantasystem.milktea.data.infrastructure.note.timeline

import net.pantasystem.milktea.model.note.timeline.TimelineResponse

interface TimelineLocalSourceLoader {

    suspend fun getFromCache(
        accountId: Long,
        pageId: Long,
        untilId: String?,
        sinceId: String?,
        limit: Int
    ): Result<TimelineResponse>

}