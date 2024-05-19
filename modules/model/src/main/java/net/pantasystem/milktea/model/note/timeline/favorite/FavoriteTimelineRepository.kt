package net.pantasystem.milktea.model.note.timeline.favorite

import net.pantasystem.milktea.model.note.Note

interface FavoriteTimelineRepository {
    suspend fun findPreviousTimeline(
        accountId: Long,
        untilId: String? = null,
        untilDate: Long? = null,
        limit: Int = 10,
    ): Result<FavoriteTimelineResponse>

    /**
     * @Param sinceDate はサーバーによっては使えない場合がある
     */
    suspend fun findLaterTimeline(
        accountId: Long,
        sinceId: String? = null,
        sinceDate: Long? = null,
        limit: Int = 10,
    ): Result<FavoriteTimelineResponse>

}

data class FavoriteTimelineResponse(
    val timelineItems: List<Note.Id>,
    val sinceId: String?,
    val untilId: String?,
)