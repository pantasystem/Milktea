package net.pantasystem.milktea.data.infrastructure.note.timeline

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.mapCancellableCatching
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.NoteRepository
import net.pantasystem.milktea.model.note.timeline.TimelineRepository
import net.pantasystem.milktea.model.note.timeline.TimelineResponse
import net.pantasystem.milktea.model.note.timeline.TimelineType
import javax.inject.Inject

class TimelineRepositoryImpl @Inject constructor(
    private val accountRepository: AccountRepository,
    private val timelineCacheDAO: TimelineCacheDAO,
    private val applicationScope: CoroutineScope,
    private val noteRepository: NoteRepository,
    private val loggerFactory: Logger.Factory,
    private val timelineFetcher: TimelineFetcher,
    private val timelineLocalDataSource: TimelineLocalDataSource,
) : TimelineRepository {

    private val logger by lazy {
        loggerFactory.create("TimelineRepository")
    }

    override suspend fun findLaterTimeline(
        type: TimelineType,
        sinceId: String?,
        sinceDate: Long?,
        limit: Int
    ): Result<TimelineResponse> = runCancellableCatching {
        if (!type.isAllowPageable()) {
            throw IllegalArgumentException("pageable is not allowed")
        }

        val account = accountRepository.get(type.accountId).getOrThrow()

        if (type.canCache() && sinceDate == null) {
            val inCache = getFromCache(
                type.accountId,
                type.pageId!!,
                null,
                sinceId,
                limit
            ).getOrThrow()

            logger.debug {
                "findLaterTimeline: inCache.size=${inCache.timelineItems.size} limit=$limit"
            }
            if (inCache.timelineItems.size >= limit) {
                applicationScope.launch {
                    if (type.canCache()) {
                        fetchTimeline(
                            account,
                            type.pageable,
                            null,
                            sinceId,
                            null,
                            null,
                            limit
                        ).mapCancellableCatching { response ->
                            saveToCache(
                                accountId = account.accountId,
                                pageId = type.pageId!!,
                                response.timelineItems.map { it.noteId },
                            ).getOrThrow()
                        }.onFailure {
                            logger.error("failed sync timeline to cache", it)
                        }
                    }
                }
                return@runCancellableCatching inCache
            }
        }

        val res = fetchTimeline(
            account,
            type.pageable,
            null,
            sinceId,
            null,
            sinceDate,
            limit
        ).getOrThrow()
        if (type.canCache() && type.pageId != null && sinceDate == null) {
            saveToCache(
                accountId = account.accountId,
                pageId = type.pageId!!,
                res.timelineItems.map { it.noteId }).getOrThrow()
        }

        res
    }

    override suspend fun findPreviousTimeline(
        type: TimelineType,
        untilId: String?,
        untilDate: Long?,
        limit: Int
    ): Result<TimelineResponse> = runCancellableCatching {
        if (!type.isAllowPageable()) {
            throw IllegalArgumentException("pageable is not allowed")
        }

        val account = accountRepository.get(type.accountId).getOrThrow()

        if (type.canCache() && untilDate == null) {
            val inCache = getFromCache(
                type.accountId,
                type.pageId!!,
                untilId,
                null,
                limit
            ).getOrThrow()

            logger.debug {
                "findPreviousTimeline: inCache.size=${inCache.timelineItems.size} limit=$limit"
            }
            if (inCache.timelineItems.size >= limit) {
                applicationScope.launch {
                    val lastItemId = inCache.timelineItems.lastOrNull()
                    if (type.canCache()) {
                        fetchTimeline(
                            account,
                            type.pageable,
                            untilId ?: lastItemId?.noteId,
                            null,
                            null,
                            null,
                            limit
                        ).mapCancellableCatching { response ->
                            saveToCache(
                                accountId = account.accountId,
                                pageId = type.pageId!!,
                                response.timelineItems.map { it.noteId },
                            ).getOrThrow()
                        }.onFailure {
                            logger.error("failed sync timeline to cache", it)
                        }
                    }
                    if (untilId == null && lastItemId != null) {
                        noteRepository.sync(lastItemId)
                    }
                }
                return@runCancellableCatching inCache
            }
        }

        val res = fetchTimeline(
            account,
            type.pageable,
            untilId,
            null,
            untilDate,
            null,
            limit
        ).getOrThrow()
        if (type.canCache() && type.pageId != null && untilDate == null) {
            saveToCache(
                accountId = account.accountId,
                pageId = type.pageId!!,
                res.timelineItems.map { it.noteId }).getOrThrow()
        }

        res
    }

    override suspend fun add(type: TimelineType, noteId: Note.Id): Result<Unit> =
        runCancellableCatching {
            if (!type.canCache()) {
                return@runCancellableCatching
            }

            val account = accountRepository.get(type.accountId).getOrThrow()
            saveToCache(
                accountId = account.accountId,
                pageId = type.pageId!!,
                listOf(noteId.noteId)
            )
        }

    override suspend fun clear(type: TimelineType): Result<Unit> = runCancellableCatching {
        if (!type.canCache()) {
            return@runCancellableCatching
        }
        timelineCacheDAO.clear(type.accountId, type.pageId!!)
    }

    override suspend fun findFirstLaterId(type: TimelineType): Result<String?> =
        runCancellableCatching {
            if (!type.canCache()) {
                return@runCancellableCatching null
            }
            timelineCacheDAO.findFirstLaterId(type.accountId, type.pageId!!)
        }

    override suspend fun findLastPreviousId(type: TimelineType): Result<String?> =
        runCancellableCatching {
            if (!type.canCache()) {
                return@runCancellableCatching null
            }
            timelineCacheDAO.findLastPreviousId(type.accountId, type.pageId!!)
        }

    private suspend fun fetchTimeline(
        account: Account,
        pageable: Pageable,
        untilId: String?,
        sinceId: String?,
        untilDate: Long?,
        sinceDate: Long?,
        limit: Int
    ): Result<TimelineResponse> = timelineFetcher.fetchTimeline(
        account = account,
        pageable = pageable,
        untilId = untilId,
        sinceId = sinceId,
        untilDate = untilDate,
        sinceDate = sinceDate,
        limit = limit
    )

    private suspend fun getFromCache(
        accountId: Long,
        pageId: Long,
        untilId: String?,
        sinceId: String?,
        limit: Int
    ): Result<TimelineResponse> = timelineLocalDataSource.getFromCache(
        accountId = accountId,
        pageId = pageId,
        untilId = untilId,
        sinceId = sinceId,
        limit = limit,
    )


    private suspend fun saveToCache(
        accountId: Long,
        pageId: Long,
        timelineItems: List<String>,
    ): Result<Unit> = timelineLocalDataSource.saveToCache(accountId, pageId, timelineItems)


}
