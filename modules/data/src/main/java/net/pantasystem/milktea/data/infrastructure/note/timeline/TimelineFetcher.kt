package net.pantasystem.milktea.data.infrastructure.note.timeline

import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.note.timeline.TimelineResponse

interface TimelineFetcher {
    suspend fun fetchTimeline(
        account: Account,
        pageable: Pageable,
        untilId: String?,
        sinceId: String?,
        untilDate: Long?,
        sinceDate: Long?,
        limit: Int
    ): Result<TimelineResponse>
}