package net.pantasystem.milktea.data.infrastructure.note.timeline

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.pantasystem.milktea.api.mastodon.status.TootStatusDTO
import net.pantasystem.milktea.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.api.misskey.notes.NoteRequest
import net.pantasystem.milktea.common.MastodonLinkHeaderDecoder
import net.pantasystem.milktea.common.mapCancellableCatching
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIFactory
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.note.NoteDataSourceAdder
import net.pantasystem.milktea.data.infrastructure.note.impl.sqlite.NoteEntity
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import net.pantasystem.milktea.model.nodeinfo.NodeInfoRepository
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.NoteRepository
import net.pantasystem.milktea.model.note.timeline.TimelineRepository
import net.pantasystem.milktea.model.note.timeline.TimelineResponse
import net.pantasystem.milktea.model.note.timeline.TimelineType
import retrofit2.Response
import javax.inject.Inject

class TimelineRepositoryImpl @Inject constructor(
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val mastodonAPIFactory: MastodonAPIFactory,
    private val noteDataSourceAdder: NoteDataSourceAdder,
    private val accountRepository: AccountRepository,
    private val timelineCacheDAO: TimelineCacheDAO,
    private val nodeInfoRepository: NodeInfoRepository,
    private val applicationScope: CoroutineScope,
    private val noteRepository: NoteRepository,
) : TimelineRepository {

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
                            )
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
            saveToCache(accountId = account.accountId, pageId = type.pageId!!, res.timelineItems.map { it.noteId }).getOrThrow()
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
                            )
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
            saveToCache(accountId = account.accountId, pageId = type.pageId!!, res.timelineItems.map { it.noteId }).getOrThrow()
        }

        res
    }

    override suspend fun add(type: TimelineType, noteId: Note.Id): Result<Unit> =
        runCancellableCatching {
            if (!type.canCache()) {
                return@runCancellableCatching
            }

            val account = accountRepository.get(type.accountId).getOrThrow()
            saveToCache(accountId = account.accountId, pageId = type.pageId!!, listOf(noteId.noteId))
        }

    override suspend fun clear(type: TimelineType): Result<Unit> = runCancellableCatching {
        if (!type.canCache()) {
            return@runCancellableCatching
        }
        timelineCacheDAO.clear(type.accountId, type.pageId!!)
    }

    private suspend fun fetchTimeline(
        account: Account,
        pageable: Pageable,
        untilId: String?,
        sinceId: String?,
        untilDate: Long?,
        sinceDate: Long?,
        limit: Int
    ): Result<TimelineResponse> = runCancellableCatching {
        when (account.instanceType) {
            Account.InstanceType.MISSKEY, Account.InstanceType.FIREFISH -> {
                val res = fetchTimelineForMisskey(
                    account,
                    pageable,
                    untilId,
                    sinceId,
                    untilDate,
                    sinceDate,
                    limit,
                ).getOrThrow()
                val notes = noteDataSourceAdder.addNoteDtoListToDataSource(account, res.filter {
                    it.promotionId == null || it.tmpFeaturedId == null
                })
                val ids = notes.sortedByDescending {
                    it.noteId
                }
                TimelineResponse(
                    ids,
                    sinceId = ids.firstOrNull()?.noteId,
                    untilId = ids.lastOrNull()?.noteId
                )
            }

            Account.InstanceType.PLEROMA, Account.InstanceType.MASTODON -> {

                val res = fetchTimelineForMastodon(
                    account,
                    pageable,
                    untilId,
                    sinceId,
                )
                val ids =
                    noteDataSourceAdder.addTootStatusDtoListIntoDataSource(account, res.body()!!)

                val minMaxId = if (isShouldUseLinkHeader(pageable)) {
                    getMaxOrMinId(res)

                } else {
                    val minId = ids.minByOrNull { it.noteId }?.noteId
                    val maxId = ids.maxByOrNull { it.noteId }?.noteId
                    MaxAndMinId(maxId, minId)
                }

                TimelineResponse(
                    ids,
                    sinceId = minMaxId.minId,
                    untilId = minMaxId.maxId
                )
            }
        }
    }

    private suspend fun fetchTimelineForMisskey(
        account: Account,
        pageable: Pageable,
        untilId: String?,
        sinceId: String?,
        untilDate: Long?,
        sinceDate: Long?,
        limit: Int,
    ): Result<List<NoteDTO>> {
        val api = misskeyAPIProvider.get(account)
        val closure = when (pageable) {
            is Pageable.GlobalTimeline -> api::globalTimeline
            is Pageable.LocalTimeline -> api::localTimeline
            is Pageable.HybridTimeline -> api::hybridTimeline
            is Pageable.HomeTimeline -> api::homeTimeline
            is Pageable.Search -> api::searchNote
            is Pageable.Favorite -> throw IllegalArgumentException("use FavoriteNotePagingStore.kt")
            is Pageable.UserTimeline -> api::userNotes
            is Pageable.UserListTimeline -> api::userListTimeline
            is Pageable.SearchByTag -> api::searchByTag
            is Pageable.Featured -> api::featured
            is Pageable.Mention -> api::mentions
            is Pageable.CalckeyRecommendedTimeline -> api::getCalckeyRecommendedTimeline
            is Pageable.ClipNotes -> api::getClipNotes
            is Pageable.Antenna -> (api)::antennasNotes
            is Pageable.ChannelTimeline -> api::channelTimeline
            else -> throw IllegalArgumentException("unknown class:${pageable.javaClass}")
        }
        val builder = NoteRequest.Builder(
            i = account.token,
            pageable = pageable,
            limit = limit,
        )
        val req = builder.build(
            NoteRequest.Conditions(
                untilId = untilId,
                sinceId = sinceId,
                untilDate = untilDate,
                sinceDate = sinceDate,
            )
        )
        return runCancellableCatching {
            requireNotNull(closure(req).throwIfHasError().body())
        }
    }

    private suspend fun fetchTimelineForMastodon(
        account: Account,
        pageableTimeline: Pageable,
        maxId: String?,
        minId: String?,
    ): Response<List<TootStatusDTO>> {
        val api = mastodonAPIFactory.build(account.normalizedInstanceUri, account.token)
        return when (pageableTimeline) {
            is Pageable.Mastodon.HashTagTimeline -> api.getHashtagTimeline(
                pageableTimeline.hashtag,
                maxId = maxId,
                minId = minId,
                onlyMedia = pageableTimeline.getOnlyMedia()
            )

            is Pageable.Mastodon.HomeTimeline -> api.getHomeTimeline(
                maxId = maxId,
                minId = minId,
                visibilities = getVisibilitiesParameter(account)
            )

            is Pageable.Mastodon.ListTimeline -> api.getListTimeline(
                maxId = maxId,
                listId = pageableTimeline.listId,
            )

            is Pageable.Mastodon.LocalTimeline -> api.getPublicTimeline(
                local = true,
                maxId = maxId,
                visibilities = getVisibilitiesParameter(account),
                onlyMedia = pageableTimeline.getOnlyMedia()
            )

            is Pageable.Mastodon.PublicTimeline -> api.getPublicTimeline(
                maxId = maxId,
                visibilities = getVisibilitiesParameter(account),
                onlyMedia = pageableTimeline.getOnlyMedia()
            )

            is Pageable.Mastodon.UserTimeline -> {
                api.getAccountTimeline(
                    accountId = pageableTimeline.userId,
                    onlyMedia = pageableTimeline.isOnlyMedia,
                    excludeReplies = pageableTimeline.excludeReplies,
                    excludeReblogs = pageableTimeline.excludeReblogs,
                    maxId = maxId,
                )
            }

            Pageable.Mastodon.BookmarkTimeline -> {
                api.getBookmarks(
                    maxId = maxId,
                )
            }

            else -> throw IllegalArgumentException("unknown class:${pageableTimeline.javaClass}")
        }
    }

    private suspend fun getFromCache(
        accountId: Long,
        pageId: Long,
        untilId: String?,
        sinceId: String?,
        limit: Int
    ): Result<TimelineResponse> = runCancellableCatching {
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
            sinceId = localItems.firstOrNull()?.noteLocalId,
            untilId = localItems.lastOrNull()?.noteLocalId
        )

    }


    private suspend fun saveToCache(
        accountId: Long,
        pageId: Long,
        timelineItems: List<String>,
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

    private fun getMaxOrMinId(response: Response<*>): MaxAndMinId {
        val decoder = MastodonLinkHeaderDecoder(response.headers()["link"])

        return MaxAndMinId(
            maxId = decoder.getMaxId(),
            minId = decoder.getMinId()
        )
    }

    private suspend fun getVisibilitiesParameter(account: Account): List<String>? {
        val nodeInfo = nodeInfoRepository.find(account.getHost()).getOrNull() ?: return null
        return if (nodeInfo.type is NodeInfo.SoftwareType.Mastodon.Fedibird || nodeInfo.type is NodeInfo.SoftwareType.Mastodon.Kmyblue) {
            listOf("public", "unlisted", "private", "limited", "direct", "personal")
        } else {
            null
        }
    }

    private fun isShouldUseLinkHeader(pageableTimeline: Pageable): Boolean {
        return pageableTimeline is Pageable.Mastodon.BookmarkTimeline
                || pageableTimeline is Pageable.Mastodon.UserTimeline
                || pageableTimeline is Pageable.Mastodon.Mention
    }


}

data class MaxAndMinId(
    val maxId: String?,
    val minId: String?
)



