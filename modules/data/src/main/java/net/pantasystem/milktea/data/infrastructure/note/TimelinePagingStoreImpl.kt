package net.pantasystem.milktea.data.infrastructure.note

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import net.pantasystem.milktea.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.api.misskey.notes.NoteRequest
import net.pantasystem.milktea.app_store.notes.InitialLoadQuery
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.paginator.EntityConverter
import net.pantasystem.milktea.common.paginator.FutureLoader
import net.pantasystem.milktea.common.paginator.IdGetter
import net.pantasystem.milktea.common.paginator.PreviousLoader
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.account.page.SincePaginate
import net.pantasystem.milktea.model.account.page.UntilPaginate
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.timeline.TimelineRepository
import net.pantasystem.milktea.model.note.timeline.TimelineType
import retrofit2.Response


internal class TimelinePagingStoreImpl(
    private val pageableTimeline: Pageable,
    private val getAccount: suspend () -> Account,
    private val getInitialLoadQuery: () -> InitialLoadQuery?,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val timelineRepository: TimelineRepository,
    private val pageId: Long? = null,
) : EntityConverter<Note.Id, Note.Id>, PreviousLoader<Note.Id>, FutureLoader<Note.Id>,
    IdGetter<Note.Id>, TimelinePagingBase, StreamingReceivableStore {

    private val _state =
        MutableStateFlow<PageableState<List<Note.Id>>>(PageableState.Loading.Init())
    override val state: Flow<PageableState<List<Note.Id>>>
        get() = _state

    override val mutex: Mutex = Mutex()

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

    override suspend fun loadPrevious(): Result<List<Note.Id>> {
        return runCancellableCatching {
            val untilId = getUntilId()?.noteId
            if (pageableTimeline !is UntilPaginate && untilId != null) {
                return@runCancellableCatching emptyList()
            }
            val initialLoadQuery = getInitialLoadQuery.invoke()
            val untilDate = (initialLoadQuery as? InitialLoadQuery.UntilDate)?.date
            timelineRepository.findPreviousTimeline(
                TimelineType(
                    getAccount.invoke().accountId,
                    pageableTimeline,
                    pageId,
                ),
                untilId = untilId
                    ?: (initialLoadQuery as? InitialLoadQuery.UntilId)?.noteId?.noteId,
                untilDate = if (untilId == null) untilDate?.toEpochMilliseconds() else null,
            ).getOrThrow().timelineItems

        }
    }

    override suspend fun loadFuture(): Result<List<Note.Id>> {
        return runCancellableCatching {
            if (pageableTimeline !is SincePaginate) {
                return@runCancellableCatching emptyList()
            }

            val builder = NoteRequest.Builder(
                i = getAccount.invoke().token,
                pageable = pageableTimeline,
                limit = LIMIT
            )
            val req = builder.build(NoteRequest.Conditions(sinceId = getSinceId()?.noteId))

            // MisskeyはsinceIdで取得した場合
            // [1, 2, 3, 4, 5]という順番で取得されるようになっていた。
            // しかしアプリ上では[5, 4, 3, 2, 1]という順番で扱えた方が好ましいため、
            // FuturePagingControllerではasReverseする処理を行っている。
            // しかしMisskey側の変更によって[5, 4, 3, 2, 1]という順番で取得されるようになったため、
            // sortedByをして、ソート順が変わっても対応できるようにしている。
            timelineRepository.findLaterTimeline(
                TimelineType(
                    getAccount.invoke().accountId,
                    pageableTimeline,
                    pageId,
                ),
                sinceId = getSinceId()?.noteId,
            ).getOrThrow().timelineItems.sortedBy {
                it.noteId
            }
        }
    }

    override suspend fun convertAll(list: List<Note.Id>): List<Note.Id> {
        return list
    }

    override fun getState(): PageableState<List<Note.Id>> {
        return _state.value
    }

    override fun setState(state: PageableState<List<Note.Id>>) {
        _state.value = state
    }

    private suspend fun getStore(): (suspend (NoteRequest) -> Response<List<NoteDTO>?>)? {
        val account = getAccount.invoke()
        val api = misskeyAPIProvider.get(account)
        return try {
            when (pageableTimeline) {
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
                else -> throw IllegalArgumentException("unknown class:${pageableTimeline.javaClass}")
            }

        } catch (e: NullPointerException) {
            null
        }

    }
}

