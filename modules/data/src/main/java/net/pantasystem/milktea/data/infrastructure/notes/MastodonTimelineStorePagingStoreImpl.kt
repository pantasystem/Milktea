package net.pantasystem.milktea.data.infrastructure.notes

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
import net.pantasystem.milktea.model.notes.Note


class MastodonTimelineStorePagingStoreImpl(
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
        if (getSinceId() == null && (pageableTimeline is Pageable.Mastodon.UserTimeline)) {
            if (!(getState().content as? StateContent.Exist)?.rawContent.isNullOrEmpty()) {
                return@runCancellableCatching emptyList()
            }
        }
        when (pageableTimeline) {
            is Pageable.Mastodon.HashTagTimeline -> api.getHashtagTimeline(
                pageableTimeline.hashtag,
                minId = getSinceId(),
            )
            Pageable.Mastodon.HomeTimeline -> api.getHomeTimeline(
                minId = getSinceId(),
                visibilities = getVisibilitiesParameter(getAccount())
            )
            is Pageable.Mastodon.ListTimeline -> api.getListTimeline(
                minId = getSinceId(),
                listId = pageableTimeline.listId,
            )
            is Pageable.Mastodon.LocalTimeline -> api.getPublicTimeline(
                local = true,
                minId = getSinceId(),
                visibilities = getVisibilitiesParameter(getAccount())
            )
            is Pageable.Mastodon.PublicTimeline -> api.getPublicTimeline(
                minId = getSinceId(),
                visibilities = getVisibilitiesParameter(getAccount())
            )
            is Pageable.Mastodon.UserTimeline -> {
                api.getAccountTimeline(
                    accountId = pageableTimeline.userId,
                    onlyMedia = pageableTimeline.isOnlyMedia ?: false,
                    excludeReplies = pageableTimeline.excludeReplies,
                    excludeReblogs = pageableTimeline.excludeReblogs,
                    minId = getSinceId()
                ).throwIfHasError().also {
                    val decoder = MastodonLinkHeaderDecoder(it.headers()["link"])
                    this@MastodonTimelineStorePagingStoreImpl.minId = decoder.getMinId()
                    if (this@MastodonTimelineStorePagingStoreImpl.maxId == null) {
                        decoder.getMaxId()
                    }
                }
            }
        }.throwIfHasError().body()!!
    }

    override suspend fun getSinceId(): String? {
        return minId ?: (getState().content as? StateContent.Exist)?.rawContent?.maxByOrNull {
            it.noteId
        }?.noteId
    }

    override suspend fun getUntilId(): String? {
        return maxId ?: (getState().content as? StateContent.Exist)?.rawContent?.minByOrNull {
            it.noteId
        }?.noteId
    }

    override fun setState(state: PageableState<List<Note.Id>>) {
        _state.value = state
    }

    override fun getState(): PageableState<List<Note.Id>> {
        return _state.value
    }


    override suspend fun loadPrevious(): Result<List<TootStatusDTO>> = runCancellableCatching {
        val api = mastodonAPIProvider.get(getAccount())
        val maxId = getUntilId()
        if (maxId == null && (pageableTimeline is Pageable.Mastodon.UserTimeline)) {
            if (!(getState().content as? StateContent.Exist)?.rawContent.isNullOrEmpty()) {
                return@runCancellableCatching emptyList()
            }
        }
        when (pageableTimeline) {
            is Pageable.Mastodon.HashTagTimeline -> api.getHashtagTimeline(
                pageableTimeline.hashtag,
                maxId = maxId
            )
            Pageable.Mastodon.HomeTimeline -> api.getHomeTimeline(
                maxId = maxId,
                visibilities = getVisibilitiesParameter(getAccount())
            )
            is Pageable.Mastodon.ListTimeline -> api.getListTimeline(
                maxId = maxId,
                listId = pageableTimeline.listId,
                )
            is Pageable.Mastodon.LocalTimeline -> api.getPublicTimeline(
                local = true,
                maxId = maxId,
                visibilities = getVisibilitiesParameter(getAccount())
            )
            is Pageable.Mastodon.PublicTimeline -> api.getPublicTimeline(
                maxId = maxId,
                visibilities = getVisibilitiesParameter(getAccount())
            )
            is Pageable.Mastodon.UserTimeline -> {
                api.getAccountTimeline(
                    accountId = pageableTimeline.userId,
                    onlyMedia = pageableTimeline.isOnlyMedia,
                    excludeReplies = pageableTimeline.excludeReplies,
                    excludeReblogs = pageableTimeline.excludeReblogs,
                    maxId = maxId,
                ).throwIfHasError().also {
                    val decoder = MastodonLinkHeaderDecoder(it.headers()["link"])
                    this@MastodonTimelineStorePagingStoreImpl.maxId = decoder.getMaxId()
                    if (this@MastodonTimelineStorePagingStoreImpl.minId == null) {
                        decoder.getMinId()
                    }
                }
            }
        }.throwIfHasError().body()!!
    }

    private suspend fun getVisibilitiesParameter(account: Account): List<String>? {
        val nodeInfo = nodeInfoRepository.find(account.getHost()).getOrNull() ?: return null
        return if (nodeInfo.type is NodeInfo.SoftwareType.Mastodon.Fedibird) {
            listOf("public", "unlisted", "private", "limited", "direct", "personal")
        } else {
            null
        }
    }

}