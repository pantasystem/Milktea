package net.pantasystem.milktea.model.note.timeline

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.account.AccountRepository
import javax.inject.Inject

/**
 * 一番先頭の投稿から、キャッシュ上にある最新の投稿に追いつくまで取得し続ける
 */
class SyncTimelineFromLatestToCurrentUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val timelineRepository: TimelineRepository,
) {

    suspend operator fun invoke(): Result<Unit> = runCancellableCatching {
        val accounts = accountRepository.findAll().getOrThrow()
        coroutineScope {
            accounts.flatMap { it.pages }.map {
                async {
                    val type = TimelineType(
                        accountId = it.accountId,
                        pageable = it.pageable(),
                        pageId = it.pageId,
                    )
                    if (type.canCache() && it.isSavePagePosition) {
                        val currentId = timelineRepository.findLastPreviousId(type).getOrThrow()
                        sync(
                            type,
                            nextId = null,
                            currentId,
                        ).getOrThrow()
                    }
                }
            }.awaitAll()
        }
    }

    //
    suspend fun sync(type: TimelineType, nextId: String?, currentId: String?, loopCount: Int = 0): Result<Unit> = runCancellableCatching {
        if (!type.canCache()) {
            return@runCancellableCatching
        }
        val response = timelineRepository.findPreviousTimeline(type, untilId = nextId).getOrThrow()
        if (response.timelineItems.isEmpty()) {
            return@runCancellableCatching
        }

        val lastItemId = response.timelineItems.last().noteId
        // 最後にある要素がcurrentIdより小さくなれば終了
        // ページ数が100ページを越えれば終了
        // 100という数字に特に意味はないが10 * 100で1000投稿になり流石にそれ以上の投稿をユーザが読むとは思えないため
        if (currentId != null && lastItemId < currentId || loopCount > 100) {
            return@runCancellableCatching
        }

        delay(100)
        sync(type, response.untilId, currentId = currentId, loopCount + 1)
    }
}