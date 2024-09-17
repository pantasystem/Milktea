package net.pantasystem.milktea.data.infrastructure.note.timeline

import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.data.infrastructure.note.impl.sqlite.NoteEntity
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.timeline.TimelineResponse
import javax.inject.Inject

internal class TimelineLocalDataSourceImpl @Inject constructor(
    private val timelineCacheDAO: TimelineCacheDAO,
): TimelineLocalDataSource {
    override suspend fun saveToCache(
        accountId: Long,
        pageId: Long,
        timelineItems: List<String>
    ): Result<Unit> = runCancellableCatching {
        timelineCacheDAO.insertAll(
            timelineItems.map {
                TimelineItemEntity(
                    accountId = accountId,
                    pageId = pageId,
                    noteId = it,
                    noteLocalId = NoteEntity.makeEntityId(accountId, it)
                )
            }
        )
    }

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