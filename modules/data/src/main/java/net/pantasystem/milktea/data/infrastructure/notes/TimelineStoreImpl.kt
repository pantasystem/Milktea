package net.pantasystem.milktea.data.infrastructure.notes

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.api.misskey.favorite.Favorite
import net.pantasystem.milktea.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.api.misskey.notes.NoteRequest
import net.pantasystem.milktea.api.misskey.v12.MisskeyAPIV12
import net.pantasystem.milktea.app_store.notes.InitialLoadQuery
import net.pantasystem.milktea.app_store.notes.TimelineStore
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.paginator.*
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.account.page.SincePaginate
import net.pantasystem.milktea.model.account.page.UntilPaginate
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteDataSource
import net.pantasystem.milktea.model.notes.NoteRelation
import net.pantasystem.milktea.model.notes.NoteRelationGetter
import retrofit2.Response
import javax.inject.Inject


const val LIMIT = 10

class TimelineStoreImpl(
    private val pageableTimeline: Pageable,
    private val noteAdder: NoteDataSourceAdder,
    noteDataSource: NoteDataSource,
    private val getAccount: suspend () -> Account,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    coroutineScope: CoroutineScope,
    private val noteRelationGetter: NoteRelationGetter,
    private val metaRepository: MetaRepository,
) : TimelineStore {

    class Factory @Inject constructor(
        private val noteAdder: NoteDataSourceAdder,
        private val noteDataSource: NoteDataSource,
        private val misskeyAPIProvider: MisskeyAPIProvider,
        private val noteRelationGetter: NoteRelationGetter,
        private val metaRepository: MetaRepository,
    ) : TimelineStore.Factory {
        override fun create(
            pageable: Pageable,
            coroutineScope: CoroutineScope,
            getAccount: suspend () -> Account
        ): TimelineStore {
            return TimelineStoreImpl(
                pageable,
                noteAdder,
                noteDataSource,
                getAccount,
                misskeyAPIProvider,
                coroutineScope,
                noteRelationGetter,
                metaRepository,
            )
        }
    }

    private val willAddNoteQueue = MutableSharedFlow<Note.Id>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 1000
    )

    override val receiveNoteQueue: SharedFlow<Note.Id>
        get() = willAddNoteQueue


    val pageableStore: TimelinePagingBase by lazy {
        when (pageableTimeline) {
            is Pageable.Favorite -> {
                FavoriteNoteTimelinePagingStoreImpl(
                    pageableTimeline, noteAdder, getAccount, misskeyAPIProvider
                )
            }
            else -> TimelinePagingStoreImpl(
                pageableTimeline, noteAdder, getAccount, {
                    initialLoadQuery
                }, misskeyAPIProvider,
                metaRepository,
            )
        }
    }
    override val timelineState: Flow<PageableState<List<Note.Id>>>
        get() = pageableStore.state.distinctUntilChanged()

    var latestReceiveId: Note.Id? = null

    var initialLoadQuery: InitialLoadQuery? = null

    init {
        coroutineScope.launch(Dispatchers.IO) {
            willAddNoteQueue.collect { noteId ->
                appendStreamEventNote(noteId)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val relatedNotes: Flow<PageableState<List<NoteRelation>>> =
        noteDataSource.state.flatMapLatest {
            timelineState.map { pageableState ->
                pageableState.suspendConvert { list ->
                    noteRelationGetter.getIn(list.distinct())
                }
            }
        }.distinctUntilChanged()


    override suspend fun loadFuture(): Result<Unit> {
        return runCancellableCatching {
            val addedCount = when (val store = pageableStore) {
                is TimelinePagingStoreImpl -> {
                    FuturePagingController(
                        store,
                        store,
                        store,
                        store,
                    ).loadFuture()
                }
                is FavoriteNoteTimelinePagingStoreImpl -> {
                    FuturePagingController(
                        store,
                        store,
                        store,
                        store,
                    ).loadFuture()
                }
            }
            if (addedCount.getOrElse { Int.MAX_VALUE } < LIMIT) {
                initialLoadQuery = null
            }
            latestReceiveId = null
        }
    }

    override suspend fun loadPrevious(): Result<Unit> {
        return runCancellableCatching {
            when (val store = pageableStore) {
                is TimelinePagingStoreImpl -> {
                    PreviousPagingController(
                        store,
                        store,
                        store,
                        store,
                    ).loadPrevious()
                }
                is FavoriteNoteTimelinePagingStoreImpl -> {
                    PreviousPagingController(
                        store,
                        store,
                        store,
                        store,
                    ).loadPrevious()
                }
            }
            latestReceiveId = null
        }

    }

    override suspend fun clear(initialLoadQuery: InitialLoadQuery?) {
        pageableStore.mutex.withLock {
            this.initialLoadQuery = initialLoadQuery
            pageableStore.setState(PageableState.Loading.Init())
        }
    }


    override fun onReceiveNote(noteId: Note.Id) {
        willAddNoteQueue.tryEmit(noteId)
    }

    override fun latestReceiveNoteId(): Note.Id? {
        return latestReceiveId
    }

    private suspend fun appendStreamEventNote(noteId: Note.Id) {
        val store = pageableStore
        if (store is TimelinePagingStoreImpl) {
            store.mutex.withLock {
                if (initialLoadQuery != null) {
                    return@withLock
                }
                val content = store.getState().content

                var added = false
                store.setState(
                    PageableState.Fixed(
                        when (content) {
                            is StateContent.NotExist -> {
                                added = true
                                StateContent.Exist(
                                    listOf(noteId)
                                )
                            }
                            is StateContent.Exist -> {
                                if (content.rawContent.contains(noteId)) {
                                    content
                                } else {
                                    added = true
                                    content.copy(
                                        listOf(noteId) + content.rawContent
                                    )
                                }
                            }
                        }
                    )
                )
                if (added) {
                    latestReceiveId = noteId
                }

            }
        }
    }

}

sealed interface TimelinePagingBase : PaginationState<Note.Id>, StateLocker

class TimelinePagingStoreImpl(
    private val pageableTimeline: Pageable,
    private val noteAdder: NoteDataSourceAdder,
    private val getAccount: suspend () -> Account,
    private val getInitialLoadQuery: () -> InitialLoadQuery?,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val metaRepository: MetaRepository,
) : EntityConverter<NoteDTO, Note.Id>, PreviousLoader<NoteDTO>, FutureLoader<NoteDTO>,
    IdGetter<Note.Id>, TimelinePagingBase {

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
            val req = builder.build(NoteRequest.Conditions(
                untilId = untilId ?: (initialLoadQuery as? InitialLoadQuery.UntilId)?.noteId?.noteId,
                untilDate = if (untilId == null) untilDate?.toEpochMilliseconds() else null,
            ))
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
                is Pageable.Search ->api::searchNote
                is Pageable.Favorite -> throw IllegalArgumentException("use FavoriteNotePagingStore.kt")
                is Pageable.UserTimeline -> api::userNotes
                is Pageable.UserListTimeline -> api::userListTimeline
                is Pageable.SearchByTag -> api::searchByTag
                is Pageable.Featured -> api::featured
                is Pageable.Mention -> api::mentions
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


class FavoriteNoteTimelinePagingStoreImpl(
    val pageableTimeline: Pageable.Favorite,
    val noteAdder: NoteDataSourceAdder,
    val getAccount: suspend () -> Account,
    val misskeyAPIProvider: MisskeyAPIProvider,
) : EntityConverter<Favorite, Note.Id>, PreviousLoader<Favorite>, FutureLoader<Favorite>,
    IdGetter<String>, StateLocker, TimelinePagingBase {

    var favoriteIdAndNoteIdMap = mutableMapOf<Note.Id, String>()

    private val _state =
        MutableStateFlow<PageableState<List<Note.Id>>>(PageableState.Loading.Init())
    override val state: Flow<PageableState<List<Note.Id>>>
        get() = _state

    override val mutex: Mutex = Mutex()

    override suspend fun convertAll(list: List<Favorite>): List<Note.Id> {
        val account = getAccount.invoke()

        val fabIdAndNoteId = list.map {
            noteAdder.addNoteDtoToDataSource(account, it.note).id to it.id
        }
        favoriteIdAndNoteIdMap.putAll(fabIdAndNoteId.toMap())
        return fabIdAndNoteId.map {
            it.first
        }
    }

    override suspend fun getSinceId(): String? {
        return (getState().content as? StateContent.Exist)?.rawContent?.firstOrNull()?.let {
            favoriteIdAndNoteIdMap[it]
        }
    }

    override suspend fun getUntilId(): String? {
        return (getState().content as? StateContent.Exist)?.rawContent?.lastOrNull()?.let {
            favoriteIdAndNoteIdMap[it]
        }
    }

    override suspend fun loadFuture(): Result<List<Favorite>> {
        val ac = getAccount.invoke()
        return runCancellableCatching {
            misskeyAPIProvider.get(getAccount.invoke()).favorites(
                NoteRequest.Builder(pageableTimeline, ac.token, limit = LIMIT)
                    .build(NoteRequest.Conditions(sinceId = getSinceId()))
            ).throwIfHasError().body()!!
        }
    }

    override suspend fun loadPrevious(): Result<List<Favorite>> {
        return runCancellableCatching {
            val ac = getAccount.invoke()
            misskeyAPIProvider.get(getAccount.invoke()).favorites(
                NoteRequest.Builder(pageableTimeline, ac.token, limit = LIMIT)
                    .build(NoteRequest.Conditions(untilId = getUntilId()))
            ).throwIfHasError().body()!!
        }
    }


    override fun getState(): PageableState<List<Note.Id>> {
        return _state.value
    }

    override fun setState(state: PageableState<List<Note.Id>>) {
        _state.value = state
    }
}


