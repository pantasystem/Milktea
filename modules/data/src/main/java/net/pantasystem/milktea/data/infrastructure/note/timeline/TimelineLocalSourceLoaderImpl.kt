package net.pantasystem.milktea.data.infrastructure.note.timeline

import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.timeline.TimelineResponse
import javax.inject.Inject

internal class TimelineLocalSourceLoaderImpl @Inject constructor(
    private val timelineCacheDAO: TimelineCacheDAO,
): TimelineLocalSourceLoader {
    override suspend fun getFromCache(
        accountId: Long,
        pageId: Long,
        untilId: String?,
        sinceId: String?,
        limit: Int
    ): Result<TimelineResponse> = runCancellableCatching{
        val localItems = when {
            untilId != null && sinceId != null -> {
                timelineCacheDAO.getTimelineItemsUntilIdAndSinceId(
                    accountId,
                    pageId,
                    untilId,
                    sinceId,
                    limit
                )
            }

            untilId != null -> {
                timelineCacheDAO.getTimelineItemsUntilId(
                    accountId,
                    pageId,
                    untilId,
                    limit
                )
            }

            sinceId != null -> {
                timelineCacheDAO.getTimelineItemsSinceId(
                    accountId,
                    pageId,
                    sinceId,
                    limit
                )
            }

            else -> {
                timelineCacheDAO.getTimelineItems(
                    accountId,
                    pageId,
                    limit
                )
            }
        }
        TimelineResponse(
            localItems.map { Note.Id(it.accountId, it.noteId) },
            sinceId = localItems.firstOrNull()?.noteId,
            untilId = localItems.lastOrNull()?.noteId
        )
    }

}