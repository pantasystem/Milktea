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
import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.paginator.*
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.gettters.Getters
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteDataSource
import net.pantasystem.milktea.model.notes.NoteRelation
import net.pantasystem.milktea.model.notes.TimelineStore
import retrofit2.Response


class TimelineStoreImpl(
    val pageableTimeline: Pageable,
    val noteAdder: NoteDataSourceAdder,
    val noteDataSource: NoteDataSource,
    val getters: Getters,
    val getAccount: suspend () -> Account,
    val encryption: Encryption,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val coroutineScope: CoroutineScope,
) : TimelineStore {

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
                    pageableTimeline, noteAdder, getAccount, encryption, misskeyAPIProvider
                )
            }
            else -> TimelinePagingStoreImpl(
                pageableTimeline, noteAdder, getAccount, encryption, misskeyAPIProvider
            )
        }
    }
    override val timelineState: Flow<PageableState<List<Note.Id>>>
        get() = pageableStore.state

    var latestReceiveId: Note.Id? = null

    init {
        coroutineScope.launch(Dispatchers.IO) {
            willAddNoteQueue.collect { noteId ->
                val store = pageableStore
                if (store is TimelinePagingStoreImpl) {
                    store.mutex.withLock {
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
                                                content.rawContent.toMutableList().also { list ->
                                                    list.add(0, noteId)
                                                }
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
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val relatedNotes: Flow<PageableState<List<NoteRelation>>> =
        noteDataSource.state.flatMapLatest {
            timelineState.map { pageableState ->
                pageableState.suspendConvert { list ->
                    list.mapNotNull {
                        getters.noteRelationGetter.get(it)
                    }
                }
            }
        }


    override suspend fun loadFuture(): Result<Unit> {
        return runCatching {
            when (val store = pageableStore) {
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
            latestReceiveId = null
        }
    }

    override suspend fun loadPrevious(): Result<Unit> {
        return runCatching {
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

    override suspend fun clear() {
        pageableStore.mutex.withLock {
            pageableStore.setState(PageableState.Loading.Init())
        }
    }

    override fun onReceiveNote(noteId: Note.Id) {
        willAddNoteQueue.tryEmit(noteId)
    }

    override fun latestReceiveNoteId(): Note.Id? {
        return latestReceiveId
    }

}

sealed interface TimelinePagingBase : PaginationState<Note.Id>, StateLocker {

}

class TimelinePagingStoreImpl(
    val pageableTimeline: Pageable,
    val noteAdder: NoteDataSourceAdder,
    val getAccount: suspend () -> Account,
    val encryption: Encryption,
    val misskeyAPIProvider: MisskeyAPIProvider,
) : EntityConverter<NoteDTO, Note.Id>, PreviousLoader<NoteDTO>, FutureLoader<NoteDTO>,
    IdGetter<Note.Id>, TimelinePagingBase {

    private val _state =
        MutableStateFlow<PageableState<List<Note.Id>>>(PageableState.Loading.Init())
    override val state: Flow<PageableState<List<Note.Id>>>
        get() = _state

    override val mutex: Mutex = Mutex()

    override suspend fun getSinceId(): Note.Id? {
        return (getState().content as? StateContent.Exist)?.rawContent?.firstOrNull()
    }

    override suspend fun getUntilId(): Note.Id? {
        return (getState().content as? StateContent.Exist)?.rawContent?.lastOrNull()
    }

    override suspend fun loadPrevious(): Result<List<NoteDTO>> {
        return runCatching {
            val builder = NoteRequest.Builder(
                i = getAccount.invoke().getI(encryption),
                pageable = pageableTimeline
            )
            val req = builder.build(NoteRequest.Conditions(untilId = getUntilId()?.noteId))
            getStore()!!.invoke(req).throwIfHasError().body()!!
        }
    }

    override suspend fun loadFuture(): Result<List<NoteDTO>> {
        return runCatching {
            val builder = NoteRequest.Builder(
                i = getAccount.invoke().getI(encryption),
                pageable = pageableTimeline
            )
            val req = builder.build(NoteRequest.Conditions(sinceId = getSinceId()?.noteId))
            getStore()!!.invoke(req).throwIfHasError().body()!!
        }
    }

    override suspend fun convertAll(list: List<NoteDTO>): List<Note.Id> {
        return list.map {
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
        return try {
            when (pageableTimeline) {
                is Pageable.GlobalTimeline -> misskeyAPIProvider.get(account)::globalTimeline
                is Pageable.LocalTimeline -> misskeyAPIProvider.get(account)::localTimeline
                is Pageable.HybridTimeline -> misskeyAPIProvider.get(account)::hybridTimeline
                is Pageable.HomeTimeline -> misskeyAPIProvider.get(account)::homeTimeline
                is Pageable.Search -> misskeyAPIProvider.get(account)::searchNote
                is Pageable.Favorite -> throw IllegalArgumentException("use FavoriteNotePagingStore.kt")
                is Pageable.UserTimeline -> misskeyAPIProvider.get(account)::userNotes
                is Pageable.UserListTimeline -> misskeyAPIProvider.get(account)::userListTimeline
                is Pageable.SearchByTag -> misskeyAPIProvider.get(account)::searchByTag
                is Pageable.Featured -> misskeyAPIProvider.get(account)::featured
                is Pageable.Mention -> misskeyAPIProvider.get(account)::mentions
                is Pageable.Antenna -> {
                    val api = misskeyAPIProvider.get(account)
                    if (api is MisskeyAPIV12) {
                        (api)::antennasNotes
                    } else {
                        throw IllegalArgumentException("antennaはV12以上でなければ使用できません")
                    }
                }
                is Pageable.ChannelTimeline -> {
                    val api = misskeyAPIProvider.get(account)
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
    val encryption: Encryption,
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
        return runCatching {
            misskeyAPIProvider.get(getAccount.invoke()).favorites(
                NoteRequest.Builder(pageableTimeline, ac.getI(encryption))
                    .build(NoteRequest.Conditions(sinceId = getSinceId()))
            ).throwIfHasError().body()!!
        }
    }

    override suspend fun loadPrevious(): Result<List<Favorite>> {
        return runCatching {
            val ac = getAccount.invoke()
            misskeyAPIProvider.get(getAccount.invoke()).favorites(
                NoteRequest.Builder(pageableTimeline, ac.getI(encryption))
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


