package net.pantasystem.milktea.data.infrastructure.note

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import net.pantasystem.milktea.api.mastodon.notification.MstNotificationDTO
import net.pantasystem.milktea.api.mastodon.status.TootStatusDTO
import net.pantasystem.milktea.common.MastodonLinkHeaderDecoder
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.paginator.EntityConverter
import net.pantasystem.milktea.common.paginator.FutureLoader
import net.pantasystem.milktea.common.paginator.IdGetter
import net.pantasystem.milktea.common.paginator.PreviousLoader
import net.pantasystem.milktea.common.paginator.StateLocker
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.nodeinfo.NodeInfoRepository
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.timeline.TimelineRepository
import net.pantasystem.milktea.model.note.timeline.TimelineResponse
import net.pantasystem.milktea.model.note.timeline.TimelineType
import retrofit2.Response


internal class MastodonTimelineStorePagingStoreImpl(
    val pageableTimeline: Pageable.Mastodon,
    val mastodonAPIProvider: MastodonAPIProvider,
    val getAccount: suspend () -> Account,
    val noteAdder: NoteDataSourceAdder,
    val nodeInfoRepository: NodeInfoRepository,
    private val timelineRepository: TimelineRepository,
    private val pageId: Long?,
) : EntityConverter<Note.Id, Note.Id>, PreviousLoader<Note.Id>,
    FutureLoader<Note.Id>,
    IdGetter<String>, StateLocker, TimelinePagingBase, StreamingReceivableStore {

    private val _state =
        MutableStateFlow<PageableState<List<Note.Id>>>(PageableState.Loading.Init())
    override val state: Flow<PageableState<List<Note.Id>>>
        get() = _state
    override val mutex: Mutex = Mutex()

    private var maxId: String? = null
    private var minId: String? = null

    override suspend fun convertAll(list: List<Note.Id>): List<Note.Id> {
        return list
    }

    override suspend fun loadFuture(): Result<List<Note.Id>> = runCancellableCatching {
        val api = mastodonAPIProvider.get(getAccount())
        val minId = getSinceId()
        if (minId == null && isShouldUseLinkHeader()) {
            if (!isEmpty()) {
                return@runCancellableCatching emptyList()
            }
        }
        when (pageableTimeline) {

            is Pageable.Mastodon.HashTagTimeline -> timelineRepository.findLaterTimeline(
                TimelineType(
                    accountId = getAccount().accountId,
                    pageable = pageableTimeline,
                    pageId = pageId,
                ),
                sinceId = minId,
                limit = 20
            ).getOrThrow().timelineItems

            is Pageable.Mastodon.HomeTimeline -> timelineRepository.findLaterTimeline(
                TimelineType(
                    accountId = getAccount().accountId,
                    pageable = pageableTimeline,
                    pageId = pageId,
                ),
                sinceId = minId,
                limit = 20
            ).getOrThrow().timelineItems

            is Pageable.Mastodon.ListTimeline -> timelineRepository.findLaterTimeline(
                TimelineType(
                    accountId = getAccount().accountId,
                    pageable = pageableTimeline,
                    pageId = pageId,
                ),
                sinceId = minId,
                limit = 20
            ).getOrThrow().timelineItems

            is Pageable.Mastodon.LocalTimeline -> timelineRepository.findLaterTimeline(
                TimelineType(
                    accountId = getAccount().accountId,
                    pageable = pageableTimeline,
                    pageId = pageId,
                ),
                sinceId = minId,
                limit = 20
            ).getOrThrow().timelineItems

            is Pageable.Mastodon.PublicTimeline -> timelineRepository.findLaterTimeline(
                TimelineType(
                    accountId = getAccount().accountId,
                    pageable = pageableTimeline,
                    pageId = pageId,
                ),
                sinceId = minId,
                limit = 20
            ).getOrThrow().timelineItems

            is Pageable.Mastodon.UserTimeline -> timelineRepository.findLaterTimeline(
                TimelineType(
                    accountId = getAccount().accountId,
                    pageable = pageableTimeline,
                    pageId = pageId,
                ),
                sinceId = minId,
                limit = 20
            ).getOrThrow().also {
                updateMinIdFrom(it)
            }.timelineItems

            Pageable.Mastodon.BookmarkTimeline -> {
                timelineRepository.findLaterTimeline(
                    TimelineType(
                        accountId = getAccount().accountId,
                        pageable = pageableTimeline,
                        pageId = pageId,
                    ),
                    sinceId = minId,
                    limit = 20
                ).getOrThrow().also {
                    updateMinIdFrom(it)
                }.timelineItems
            }

            is Pageable.Mastodon.SearchTimeline -> {
                return@runCancellableCatching emptyList()
            }

            is Pageable.Mastodon.TrendTimeline -> {
                return@runCancellableCatching emptyList()
            }

            is Pageable.Mastodon.Mention -> {
                api.getNotifications(
                    minId = minId,
                    types = listOf(MstNotificationDTO.NotificationType.Mention.value),
                    excludeTypes = MstNotificationDTO.NotificationType.entries
                        .filterNot { it == MstNotificationDTO.NotificationType.Mention }
                        .map { it.value },
                ).throwIfHasError().also {
                    updateMinIdFrom(it)
                }.body()?.mapNotNull {
                    it.status
                }?.let {
                    noteAdder.addTootStatusDtoListIntoDataSource(getAccount(), it)
                } ?: throw IllegalStateException("response is null")
            }
        }.let { list ->
            if (isShouldUseLinkHeader()) {
                filterNotExistsStatuses(list)
            } else {
                list
            }
        }
    }

    override suspend fun getSinceId(): String? {
        if (isShouldUseLinkHeader()) {
            return minId
        }
        return (getState().content as? StateContent.Exist)?.rawContent?.maxByOrNull {
            it.noteId
        }?.noteId
    }

    override suspend fun getUntilId(): String? {
        if (isShouldUseLinkHeader()) {
            return maxId
        }
        return (getState().content as? StateContent.Exist)?.rawContent?.minByOrNull {
            it.noteId
        }?.noteId
    }

    override fun setState(state: PageableState<List<Note.Id>>) {
        if (state is PageableState.Loading.Init) {
            maxId = null
            minId = null
        }
        _state.value = state
    }

    override fun getState(): PageableState<List<Note.Id>> {
        return _state.value
    }


    override suspend fun loadPrevious(): Result<List<Note.Id>> = runCancellableCatching {
        val api = mastodonAPIProvider.get(getAccount())
        val maxId = getUntilId()

        if (maxId == null && isShouldUseLinkHeader()) {
            if (!isEmpty()) {
                return@runCancellableCatching emptyList()
            }
        }

        when (pageableTimeline) {
            is Pageable.Mastodon.HashTagTimeline -> timelineRepository.findPreviousTimeline(
                TimelineType(
                    accountId = getAccount().accountId,
                    pageable = pageableTimeline,
                    pageId = pageId,
                ),
                untilId = maxId,
                limit = 20
            ).getOrThrow().timelineItems

            is Pageable.Mastodon.HomeTimeline -> timelineRepository.findPreviousTimeline(
                TimelineType(
                    accountId = getAccount().accountId,
                    pageable = pageableTimeline,
                    pageId = pageId,
                ),
                untilId = maxId,
                limit = 20
            ).getOrThrow().timelineItems

            is Pageable.Mastodon.ListTimeline -> timelineRepository.findPreviousTimeline(
                TimelineType(
                    accountId = getAccount().accountId,
                    pageable = pageableTimeline,
                    pageId = pageId,
                ),
                untilId = maxId,
                limit = 20
            ).getOrThrow().timelineItems

            is Pageable.Mastodon.LocalTimeline -> timelineRepository.findPreviousTimeline(
                TimelineType(
                    accountId = getAccount().accountId,
                    pageable = pageableTimeline,
                    pageId = pageId,
                ),
                untilId = maxId,
                limit = 20
            ).getOrThrow().timelineItems

            is Pageable.Mastodon.PublicTimeline -> timelineRepository.findPreviousTimeline(
                TimelineType(
                    accountId = getAccount().accountId,
                    pageable = pageableTimeline,
                    pageId = null
                ),
                untilId = maxId,
                limit = 20
            ).getOrThrow().timelineItems

            is Pageable.Mastodon.UserTimeline -> timelineRepository.findPreviousTimeline(
                TimelineType(
                    accountId = getAccount().accountId,
                    pageable = pageableTimeline,
                    pageId = pageId,
                ),
                untilId = maxId,
                limit = 20
            ).getOrThrow().also {
                updateMaxIdFrom(it)
            }.timelineItems

            Pageable.Mastodon.BookmarkTimeline -> {
                timelineRepository.findPreviousTimeline(
                    TimelineType(
                        accountId = getAccount().accountId,
                        pageable = pageableTimeline,
                        pageId = pageId,
                    ),
                    untilId = maxId,
                    limit = 20
                ).getOrThrow().also {
                    updateMaxIdFrom(it)
                }.timelineItems
            }

            is Pageable.Mastodon.SearchTimeline -> {
                api.search(
                    q = pageableTimeline.query,
                    type = "statuses",
                    maxId = maxId,
                    offset = (getState().content as? StateContent.Exist)?.rawContent?.size ?: 0,
                    accountId = pageableTimeline.userId
                ).throwIfHasError().also {
                    updateMaxIdFrom(it)
                }.body()?.statuses?.let {
                    noteAdder.addTootStatusDtoListIntoDataSource(getAccount(), it)
                }
            }

            is Pageable.Mastodon.TrendTimeline -> {
                api.getTrendStatuses(
                    offset = (getState().content as? StateContent.Exist)?.rawContent?.size ?: 0
                ).getBodyOrFail().let {
                    noteAdder.addTootStatusDtoListIntoDataSource(getAccount(), it)
                }
            }

            is Pageable.Mastodon.Mention -> {
                api.getNotifications(
                    maxId = maxId,
                    types = listOf(MstNotificationDTO.NotificationType.Mention.value),
                    excludeTypes = MstNotificationDTO.NotificationType.entries
                        .filterNot { it == MstNotificationDTO.NotificationType.Mention }
                        .map { it.value },
                ).throwIfHasError().also {
                    updateMaxIdFrom(it)
                }.body()?.mapNotNull {
                    it.status
                }?.let {
                    noteAdder.addTootStatusDtoListIntoDataSource(getAccount(), it)
                } ?: throw IllegalStateException("response is null")
            }
        }!!.let { list ->
            if (isShouldUseLinkHeader()) {
                filterNotExistsStatuses(list)
            } else {
                list
            }
        }
    }

    private fun isEmpty(): Boolean {
        return when (val content = _state.value.content) {
            is StateContent.Exist -> content.rawContent.isEmpty()
            is StateContent.NotExist -> true
        }
    }

    private fun isShouldUseLinkHeader(): Boolean {
        return pageableTimeline is Pageable.Mastodon.BookmarkTimeline
                || pageableTimeline is Pageable.Mastodon.UserTimeline
                || pageableTimeline is Pageable.Mastodon.Mention
    }

    /**
     * 重複をフィルタする
     */
    private fun filterNotExistsStatuses(statuses: List<Note.Id>): List<Note.Id> {
        val data = (_state.value.content as? StateContent.Exist)?.rawContent
        if (data.isNullOrEmpty()) {
            return statuses
        }
        return statuses.filterNot { id ->
            data.any {
                it == id
            }
        }
    }

    /**
     * responseを元にmaxIdを更新するための関数
     * responseのmaxIdがnullの場合は更新がキャンセルされる
     * minIdがnullの場合はresponseのminIdが指定される
     */
    private fun updateMaxIdFrom(response: Response<*>) {
        val decoder = MastodonLinkHeaderDecoder(response.headers()["link"])

        // NOTE: 次のページネーションのIdが取得できない場合は次のIdが取得できるまで同じIdを使い回し続ける
        when (val nextId = decoder.getMaxId()) {
            null -> Unit
            else -> {
                maxId = nextId
            }
        }
        if (minId == null) {
            minId = decoder.getMinId()
        }
    }

    private fun updateMaxIdFrom(response: TimelineResponse) {
        // NOTE: 次のページネーションのIdが取得できない場合は次のIdが取得できるまで同じIdを使い回し続ける
        when (val nextId = response.sinceId) {
            null -> Unit
            else -> {
                maxId = nextId
            }
        }
        if (minId == null) {
            minId = response.sinceId
        }
    }

    /**
     * responseを元にminIdを得るための関数
     * responseのminIdがnullの場合は更新がキャンセルされる
     * maxIdがnullの場合はresponseのmaxIdが指定される
     */
    private fun updateMinIdFrom(response: Response<*>) {
        val decoder = MastodonLinkHeaderDecoder(response.headers()["link"])

        // NOTE: 次のページネーションのIdが取得できない場合は次のIdが取得できるまで同じIdを使い回し続ける
        when (val nextId = decoder.getMinId()) {
            null -> Unit
            else -> {
                minId = nextId
            }
        }
        if (maxId == null) {
            maxId = decoder.getMaxId()
        }
    }

    private fun updateMinIdFrom(response: TimelineResponse) {

        // NOTE: 次のページネーションのIdが取得できない場合は次のIdが取得できるまで同じIdを使い回し続ける
        when (val nextId = response.untilId) {
            null -> Unit
            else -> {
                minId = nextId
            }
        }
        if (maxId == null) {
            maxId = response.untilId
        }
    }

    private fun Response<List<TootStatusDTO>>.getBodyOrFail(): List<TootStatusDTO> {
        return requireNotNull(throwIfHasError().body())
    }
}