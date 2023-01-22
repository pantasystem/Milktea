package net.pantasystem.milktea.data.infrastructure.notes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import net.pantasystem.milktea.api.mastodon.status.TootStatusDTO
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.paginator.*
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
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
    IdGetter<Note.Id>, StateLocker, TimelinePagingBase, StreamingReceivableStore {

    private val _state =
        MutableStateFlow<PageableState<List<Note.Id>>>(PageableState.Loading.Init())
    override val state: Flow<PageableState<List<Note.Id>>>
        get() = _state
    override val mutex: Mutex = Mutex()

    override suspend fun convertAll(list: List<TootStatusDTO>): List<Note.Id> {
        val account = getAccount()
        return list.map {
            noteAdder.addTootStatusDtoIntoDataSource(account, it).id
        }
    }

    override suspend fun loadFuture(): Result<List<TootStatusDTO>> = runCancellableCatching {
        val api = mastodonAPIProvider.get(getAccount())

        when (pageableTimeline) {
            is Pageable.Mastodon.HashTagTimeline -> api.getHashtagTimeline(
                pageableTimeline.hashtag,
                minId = getSinceId()?.noteId
            )
            Pageable.Mastodon.HomeTimeline -> api.getHomeTimeline(
                minId = getSinceId()?.noteId,
                visibilities = getVisibilitiesParameter(getAccount())
            )
            is Pageable.Mastodon.ListTimeline -> api.getListTimeline(
                minId = getSinceId()?.noteId,
                listId = pageableTimeline.listId,
            )
            is Pageable.Mastodon.LocalTimeline -> api.getPublicTimeline(
                local = true,
                minId = getSinceId()?.noteId,
                visibilities = getVisibilitiesParameter(getAccount())
            )
            is Pageable.Mastodon.PublicTimeline -> api.getPublicTimeline(
                minId = getSinceId()?.noteId,
                visibilities = getVisibilitiesParameter(getAccount())
            )
        }.throwIfHasError().body()!!
    }

    override suspend fun getSinceId(): Note.Id? {
        return (getState().content as? StateContent.Exist)?.rawContent?.maxByOrNull {
            it.noteId
        }
    }

    override suspend fun getUntilId(): Note.Id? {
        return (getState().content as? StateContent.Exist)?.rawContent?.minByOrNull {
            it.noteId
        }
    }

    override fun setState(state: PageableState<List<Note.Id>>) {
        _state.value = state
    }

    override fun getState(): PageableState<List<Note.Id>> {
        return _state.value
    }


    override suspend fun loadPrevious(): Result<List<TootStatusDTO>> = runCancellableCatching {
        val api = mastodonAPIProvider.get(getAccount())
        when (pageableTimeline) {
            is Pageable.Mastodon.HashTagTimeline -> api.getHashtagTimeline(
                pageableTimeline.hashtag,
                maxId = getUntilId()?.noteId
            )
            Pageable.Mastodon.HomeTimeline -> api.getHomeTimeline(
                maxId = getUntilId()?.noteId,
                visibilities = getVisibilitiesParameter(getAccount())
            )
            is Pageable.Mastodon.ListTimeline -> api.getListTimeline(
                maxId = getUntilId()?.noteId,
                listId = pageableTimeline.listId,

                )
            is Pageable.Mastodon.LocalTimeline -> api.getPublicTimeline(
                local = true,
                maxId = getUntilId()?.noteId,
                visibilities = getVisibilitiesParameter(getAccount())
            )
            is Pageable.Mastodon.PublicTimeline -> api.getPublicTimeline(
                maxId = getUntilId()?.noteId,
                visibilities = getVisibilitiesParameter(getAccount())
            )
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