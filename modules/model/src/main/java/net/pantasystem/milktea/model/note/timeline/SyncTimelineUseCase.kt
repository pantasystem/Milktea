package net.pantasystem.milktea.model.note.timeline

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.UseCase
import net.pantasystem.milktea.model.account.AccountRepository
import javax.inject.Inject

class SyncTimelineUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val timelineRepository: TimelineRepository,
) : UseCase {

    suspend operator fun invoke(): Result<Unit> = runCancellableCatching {
        val accounts = accountRepository.findAll().getOrThrow()
        coroutineScope {
            accounts.flatMap { it.pages }.map {
                async {
                    val type = TimelineType(
                        accountId = it.accountId,
                        pageable = it.pageable(),
                        pageId = null,
                    )
                    if (type.canCache() && it.isSavePagePosition) {
                        sync(
                            type,
                            nextId = timelineRepository.findFirstLaterId(type).getOrNull()
                        )
                    }
                }
            }.awaitAll()
        }
    }

    // ページがなくなるまで最新の投稿を取得し続ける
    suspend fun sync(type: TimelineType, nextId: String?): Result<Unit> = runCancellableCatching {
        if (type.canCache()) {
            return@runCancellableCatching
        }
        val response = timelineRepository.findLaterTimeline(type, sinceId = nextId).getOrThrow()
        if (response.timelineItems.isEmpty()) {
            return@runCancellableCatching
        }

        sync(type, response.untilId)
    }
}