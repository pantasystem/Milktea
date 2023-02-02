package net.pantasystem.milktea.data.infrastructure.notes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import net.pantasystem.milktea.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.api.misskey.notes.NoteRequest
import net.pantasystem.milktea.api.misskey.v12.MisskeyAPIV12
import net.pantasystem.milktea.app_store.notes.InitialLoadQuery
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.paginator.EntityConverter
import net.pantasystem.milktea.common.paginator.FutureLoader
import net.pantasystem.milktea.common.paginator.IdGetter
import net.pantasystem.milktea.common.paginator.PreviousLoader
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.account.page.SincePaginate
import net.pantasystem.milktea.model.account.page.UntilPaginate
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.model.notes.Note
import retrofit2.Response


internal class TimelinePagingStoreImpl(
    private val pageableTimeline: Pageable,
    private val noteAdder: NoteDataSourceAdder,
    private val getAccount: suspend () -> Account,
    private val getInitialLoadQuery: () -> InitialLoadQuery?,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val metaRepository: MetaRepository,
) : EntityConverter<NoteDTO, Note.Id>, PreviousLoader<NoteDTO>, FutureLoader<NoteDTO>,
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

    override suspend fun loadPrevious(): Result<List<NoteDTO>> {
        return runCancellableCatching {
            val untilId = getUntilId()?.noteId
            if (pageableTimeline !is UntilPaginate && untilId != null) {
                return@runCancellableCatching emptyList()
            }
            val builder = NoteRequest.Builder(
                i = getAccount.invoke().token,
                pageable = pageableTimeline,
                limit = LIMIT
            )
            val initialLoadQuery = getInitialLoadQuery.invoke()
            val untilDate = (initialLoadQuery as? InitialLoadQuery.UntilDate)?.date
            val req = builder.build(
                NoteRequest.Conditions(
                    untilId = untilId
                        ?: (initialLoadQuery as? InitialLoadQuery.UntilId)?.noteId?.noteId,
                    untilDate = if (untilId == null) untilDate?.toEpochMilliseconds() else null,
                )
            )
            getStore()!!.invoke(req).throwIfHasError().body()!!
        }
    }

    override suspend fun loadFuture(): Result<List<NoteDTO>> {
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
            getStore()!!.invoke(req).throwIfHasError().body()!!
        }
    }

    override suspend fun convertAll(list: List<NoteDTO>): List<Note.Id> {
        return list.filter {
            it.promotionId == null || it.tmpFeaturedId == null
        }.map {
            noteAdder.addNoteDtoToDataSource(getAccount.invoke(), it).id
        }
    }

    override fun getState(): PageableState<List<Note.Id>> {
        return _state.value
    }

    override fun setState(state: PageableState<List<Note.Id>>) {
        _state.value = state
    }

    private suspend fun getStore(): (suspend (NoteRequest) -> Response<List<NoteDTO>?>)? {
        val account = getAccount.invoke()
        val meta = metaRepository.find(account.normalizedInstanceDomain)
        val api = misskeyAPIProvider.get(account, meta.getOrNull()?.getVersion())
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
                is Pageable.Antenna -> {
                    if (api is MisskeyAPIV12) {
                        (api)::antennasNotes
                    } else {
                        throw IllegalArgumentException("antennaはV12以上でなければ使用できません")
                    }
                }
                is Pageable.ChannelTimeline -> {
                    if (api is MisskeyAPIV12) {
                        (api)::channelTimeline
                    } else {
                        throw IllegalArgumentException("channelはV12以上でなければ使用できません")
                    }
                }
                else -> throw IllegalArgumentException("unknown class:${pageableTimeline.javaClass}")
            }

        } catch (e: NullPointerException) {
            null
        }

    }
}

