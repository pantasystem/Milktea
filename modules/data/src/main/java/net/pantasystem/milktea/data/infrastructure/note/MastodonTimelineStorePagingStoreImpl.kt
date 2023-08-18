package net.pantasystem.milktea.data.infrastructure.note

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import net.pantasystem.milktea.api.mastodon.status.TootStatusDTO
import net.pantasystem.milktea.common.*
import net.pantasystem.milktea.common.paginator.*
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import net.pantasystem.milktea.model.nodeinfo.NodeInfoRepository
import net.pantasystem.milktea.model.note.Note
import retrofit2.Response


internal class MastodonTimelineStorePagingStoreImpl(
    val pageableTimeline: Pageable.Mastodon,
    val mastodonAPIProvider: MastodonAPIProvider,
    val getAccount: suspend () -> Account,
    val noteAdder: NoteDataSourceAdder,
    val nodeInfoRepository: NodeInfoRepository,
) : EntityConverter<TootStatusDTO, Note.Id>, PreviousLoader<TootStatusDTO>,
    FutureLoader<TootStatusDTO>,
    IdGetter<String>, StateLocker, TimelinePagingBase, StreamingReceivableStore {

    private val _state =
        MutableStateFlow<PageableState<List<Note.Id>>>(PageableState.Loading.Init())
    override val state: Flow<PageableState<List<Note.Id>>>
        get() = _state
    override val mutex: Mutex = Mutex()

    private var maxId: String? = null
    private var minId: String? = null

    override suspend fun convertAll(list: List<TootStatusDTO>): List<Note.Id> {
        val account = getAccount()
        return list.map {
            noteAdder.addTootStatusDtoIntoDataSource(account, it).id
        }
    }

    override suspend fun loadFuture(): Result<List<TootStatusDTO>> = runCancellableCatching {
        val api = mastodonAPIProvider.get(getAccount())
        val minId = getSinceId()
        if (minId == null && isShouldUseLinkHeader()) {
            if (!isEmpty()) {
                return@runCancellableCatching emptyList()
            }
        }
        when (pageableTimeline) {
            is Pageable.Mastodon.HashTagTimeline -> api.getHashtagTimeline(
                pageableTimeline.hashtag,
                minId = getSinceId(),
            ).getBodyOrFail()
            Pageable.Mastodon.HomeTimeline -> api.getHomeTimeline(
                minId = getSinceId(),
                visibilities = getVisibilitiesParameter(getAccount()),
            ).getBodyOrFail()
            is Pageable.Mastodon.ListTimeline -> api.getListTimeline(
                minId = getSinceId(),
                listId = pageableTimeline.listId,
            ).getBodyOrFail()
            is Pageable.Mastodon.LocalTimeline -> api.getPublicTimeline(
                local = true,
                minId = getSinceId(),
                visibilities = getVisibilitiesParameter(getAccount()),
                onlyMedia = pageableTimeline.getOnlyMedia()
            ).getBodyOrFail()
            is Pageable.Mastodon.PublicTimeline -> api.getPublicTimeline(
                minId = getSinceId(),
                visibilities = getVisibilitiesParameter(getAccount()),
                onlyMedia = pageableTimeline.getOnlyMedia()
            ).getBodyOrFail()
            is Pageable.Mastodon.UserTimeline -> {
                api.getAccountTimeline(
                    accountId = pageableTimeline.userId,
                    onlyMedia = pageableTimeline.isOnlyMedia ?: false,
                    excludeReplies = pageableTimeline.excludeReplies,
                    excludeReblogs = pageableTimeline.excludeReblogs,
                    minId = getSinceId()
                ).throwIfHasError().also {
                    updateMinIdFrom(it)
                }.getBodyOrFail()
            }
            Pageable.Mastodon.BookmarkTimeline -> {
                api.getBookmarks(
                    minId = minId
                ).throwIfHasError().also {
                    updateMinIdFrom(it)
                }.getBodyOrFail()
            }
            is Pageable.Mastodon.SearchTimeline -> {
                return@runCancellableCatching emptyList()
            }
            is Pageable.Mastodon.TrendTimeline -> {
                return@runCancellableCatching emptyList()
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


    override suspend fun loadPrevious(): Result<List<TootStatusDTO>> = runCancellableCatching {
        val api = mastodonAPIProvider.get(getAccount())
        val maxId = getUntilId()

        if (maxId == null && isShouldUseLinkHeader()) {
            if (!isEmpty()) {
                return@runCancellableCatching emptyList()
            }
        }

        when (pageableTimeline) {
            is Pageable.Mastodon.HashTagTimeline -> api.getHashtagTimeline(
                pageableTimeline.hashtag,
                maxId = maxId,
                onlyMedia = pageableTimeline.getOnlyMedia()
            ).getBodyOrFail()
            Pageable.Mastodon.HomeTimeline -> api.getHomeTimeline(
                maxId = maxId,
                visibilities = getVisibilitiesParameter(getAccount())
            ).getBodyOrFail()
            is Pageable.Mastodon.ListTimeline -> api.getListTimeline(
                maxId = maxId,
                listId = pageableTimeline.listId,
                ).getBodyOrFail()
            is Pageable.Mastodon.LocalTimeline -> api.getPublicTimeline(
                local = true,
                maxId = maxId,
                visibilities = getVisibilitiesParameter(getAccount()),
                onlyMedia = pageableTimeline.getOnlyMedia()
            ).getBodyOrFail()
            is Pageable.Mastodon.PublicTimeline -> api.getPublicTimeline(
                maxId = maxId,
                visibilities = getVisibilitiesParameter(getAccount()),
                onlyMedia = pageableTimeline.getOnlyMedia()
            ).getBodyOrFail()
            is Pageable.Mastodon.UserTimeline -> {
                api.getAccountTimeline(
                    accountId = pageableTimeline.userId,
                    onlyMedia = pageableTimeline.isOnlyMedia,
                    excludeReplies = pageableTimeline.excludeReplies,
                    excludeReblogs = pageableTimeline.excludeReblogs,
                    maxId = maxId,
                ).throwIfHasError().also {
                    updateMaxIdFrom(it)
                }.body()
            }
            Pageable.Mastodon.BookmarkTimeline -> {
                api.getBookmarks(
                    maxId = maxId,
                ).throwIfHasError().also {
                    updateMaxIdFrom(it)
                }.body()
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
                }.body()?.statuses
            }
            is Pageable.Mastodon.TrendTimeline -> {
                api.getTrendStatuses(
                    offset = (getState().content as? StateContent.Exist)?.rawContent?.size ?: 0
                ).getBodyOrFail()
            }
        }!!.let { list ->
            if (isShouldUseLinkHeader()) {
                filterNotExistsStatuses(list)
            } else {
                list
            }
        }
    }

    private suspend fun getVisibilitiesParameter(account: Account): List<String>? {
        val nodeInfo = nodeInfoRepository.find(account.getHost()).getOrNull() ?: return null
        return if (nodeInfo.type is NodeInfo.SoftwareType.Mastodon.Fedibird) {
            listOf("public", "unlisted", "private", "limited", "direct", "personal")
        } else {
            null
        }
    }

    private fun isEmpty(): Boolean {
        return when(val content = _state.value.content) {
            is StateContent.Exist -> content.rawContent.isEmpty()
            is StateContent.NotExist -> true
        }
    }

    private fun isShouldUseLinkHeader(): Boolean {
        return pageableTimeline is Pageable.Mastodon.BookmarkTimeline
                || pageableTimeline is Pageable.Mastodon.UserTimeline
    }

    /**
     * 重複をフィルタする
     */
    private fun filterNotExistsStatuses(statuses: List<TootStatusDTO>): List<TootStatusDTO> {
        val data = (_state.value.content as? StateContent.Exist)?.rawContent
        if (data.isNullOrEmpty()) {
            return statuses
        }
        return statuses.filterNot { status ->
            data.any {
                it.noteId == status.id
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
        when(val nextId = decoder.getMaxId()) {
            null -> Unit
            else -> {
                maxId = nextId
            }
        }
        if (minId == null) {
            minId = decoder.getMinId()
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
        when(val nextId = decoder.getMinId()) {
            null -> Unit
            else -> {
                minId = nextId
            }
        }
        if (maxId == null) {
            maxId = decoder.getMaxId()
        }
    }

    private fun Response<List<TootStatusDTO>>.getBodyOrFail(): List<TootStatusDTO> {
        return requireNotNull(throwIfHasError().body())
    }
}