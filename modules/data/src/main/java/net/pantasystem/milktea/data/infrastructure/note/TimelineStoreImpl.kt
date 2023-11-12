package net.pantasystem.milktea.data.infrastructure.note

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import net.pantasystem.milktea.app_store.notes.InitialLoadQuery
import net.pantasystem.milktea.app_store.notes.TimelineStore
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.paginator.FuturePagingController
import net.pantasystem.milktea.common.paginator.PaginationState
import net.pantasystem.milktea.common.paginator.PreviousPagingController
import net.pantasystem.milktea.common.paginator.StateLocker
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.instance.InstanceInfoService
import net.pantasystem.milktea.model.nodeinfo.NodeInfoRepository
import net.pantasystem.milktea.model.note.Note
import javax.inject.Inject


const val LIMIT = 10
const val REMOVE_DIFF_COUNT = 20

class TimelineStoreImpl(
    private val pageableTimeline: Pageable,
    private val noteAdder: NoteDataSourceAdder,
    private val getAccount: suspend () -> Account,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    coroutineScope: CoroutineScope,
    private val mastodonAPIProvider: MastodonAPIProvider,
    private val nodeInfoRepository: NodeInfoRepository,
    private val instanceInfoService: InstanceInfoService,
) : TimelineStore {

    class Factory @Inject constructor(
        private val noteAdder: NoteDataSourceAdder,
        private val misskeyAPIProvider: MisskeyAPIProvider,
        private val mastodonAPIProvider: MastodonAPIProvider,
        private val nodeInfoRepository: NodeInfoRepository,
        private val instanceInfoService: InstanceInfoService,
    ) : TimelineStore.Factory {
        override fun create(
            pageable: Pageable,
            coroutineScope: CoroutineScope,
            getAccount: suspend () -> Account
        ): TimelineStore {
            return TimelineStoreImpl(
                pageable,
                noteAdder,
                getAccount,
                misskeyAPIProvider,
                coroutineScope,
                mastodonAPIProvider,
                nodeInfoRepository = nodeInfoRepository,
                instanceInfoService = instanceInfoService
            )
        }
    }

    private val willAddNoteQueue = MutableSharedFlow<Note.Id>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 1000
    )

    override val receiveNoteQueue: SharedFlow<Note.Id>
        get() = willAddNoteQueue

    private var activeStreamingChangedListener: ((Boolean) -> Unit)? = null


    internal val pageableStore: TimelinePagingBase by lazy {
        when (pageableTimeline) {
            is Pageable.Favorite -> {
                FavoriteNoteTimelinePagingStoreImpl(
                    pageableTimeline, noteAdder, getAccount, misskeyAPIProvider, mastodonAPIProvider
                )
            }
            is Pageable.Mastodon -> {
                MastodonTimelineStorePagingStoreImpl(
                    pageableTimeline = pageableTimeline,
                    mastodonAPIProvider = mastodonAPIProvider,
                    getAccount = getAccount,
                    noteAdder = noteAdder,
                    nodeInfoRepository = nodeInfoRepository
                )
            }
            else -> TimelinePagingStoreImpl(
                pageableTimeline = pageableTimeline,
                noteAdder = noteAdder,
                getAccount = getAccount,
                getCurrentInstanceInfo = {
                    instanceInfoService.find(it).getOrNull()
                },
                getInitialLoadQuery = {
                    initialLoadQuery
                },
                misskeyAPIProvider = misskeyAPIProvider,
            )
        }
    }
    override val timelineState: Flow<PageableState<List<Note.Id>>>
        get() = pageableStore.state.distinctUntilChanged()

    private var latestReceiveId: Note.Id? = null

    private var initialLoadQuery: InitialLoadQuery? = null

    override var isActiveStreaming: Boolean = true
        private set(value) {
            field = value
            activeStreamingChangedListener?.invoke(value)
        }

    init {
        coroutineScope.launch(Dispatchers.IO) {
            willAddNoteQueue.collect { noteId ->
                appendStreamEventNote(noteId)
            }
        }
    }

    override suspend fun loadFuture(): Result<Int> {
        return runCancellableCatching<Int> {
            val addedCount = when (val store = pageableStore) {
                is TimelinePagingStoreImpl -> {
                    FuturePagingController.create(
                        store,
                    ).loadFuture()
                }
                is FavoriteNoteTimelinePagingStoreImpl -> {
                    FuturePagingController.create(
                        store,
                    ).loadFuture()
                }
                is MastodonTimelineStorePagingStoreImpl -> {
                    FuturePagingController.create(
                        store,
                    ).loadFuture()
                }
            }
            if (addedCount.getOrElse { Int.MAX_VALUE } < LIMIT) {
                initialLoadQuery = null
                isActiveStreaming = true
            }
            latestReceiveId = null
            addedCount.getOrThrow()
        }
    }

    override suspend fun loadPrevious(): Result<Int> {
        return runCancellableCatching<Int> {
            val result = when (val store = pageableStore) {
                is TimelinePagingStoreImpl -> {
                    PreviousPagingController.create(
                        store,
                    ).loadPrevious()
                }
                is FavoriteNoteTimelinePagingStoreImpl -> {
                    PreviousPagingController.create(
                        store,
                    ).loadPrevious()
                }
                is MastodonTimelineStorePagingStoreImpl -> {
                    PreviousPagingController.create(
                        store,
                    ).loadPrevious()
                }
            }
            latestReceiveId = null
            result.getOrThrow()
        }

    }

    override suspend fun clear(initialLoadQuery: InitialLoadQuery?) {
        pageableStore.mutex.withLock {
            this.initialLoadQuery = initialLoadQuery
            isActiveStreaming = initialLoadQuery == null
            pageableStore.setState(PageableState.Loading.Init())
        }
    }


    override fun onReceiveNote(noteId: Note.Id) {
        willAddNoteQueue.tryEmit(noteId)
    }

    override fun latestReceiveNoteId(): Note.Id? {
        return latestReceiveId
    }

    override fun suspendStreaming() {
        isActiveStreaming = false
    }

    override fun setActiveStreamingChangedListener(listener: (Boolean) -> Unit) {
        activeStreamingChangedListener = listener
    }

    private suspend fun appendStreamEventNote(noteId: Note.Id) {
        val store = pageableStore
        if (store is StreamingReceivableStore) {
            store.mutex.withLock {
                if (initialLoadQuery != null) {
                    return@withLock
                }
                if (!isActiveStreaming) {
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

    override suspend fun releaseUnusedPages(position: Int, offset: Int) {
        releaseUnusedPage(pageableStore, position, offset, REMOVE_DIFF_COUNT)
        if (pageableStore.mutex.isLocked) {
            return
        }
    }

}



internal sealed interface TimelinePagingBase : PaginationState<Note.Id>, StateLocker

interface StreamingReceivableStore : StateLocker
