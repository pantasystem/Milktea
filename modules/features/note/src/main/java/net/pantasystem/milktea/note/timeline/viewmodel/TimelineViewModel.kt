package net.pantasystem.milktea.note.timeline.viewmodel

import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Instant
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.notes.InitialLoadQuery
import net.pantasystem.milktea.app_store.notes.TimelineStore
import net.pantasystem.milktea.common.APIError
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.coroutines.throttleLatest
import net.pantasystem.milktea.common_android.resource.StringSource
import net.pantasystem.milktea.common_android_ui.APIErrorStringConverter
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.CurrentAccountWatcher
import net.pantasystem.milktea.model.account.UnauthorizedException
import net.pantasystem.milktea.model.account.page.CanExcludeReplies
import net.pantasystem.milktea.model.account.page.CanExcludeReposts
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.NoteStreaming
import net.pantasystem.milktea.model.note.TimelineScrollPositionRepository
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewDataCache
import java.io.IOException
import java.net.SocketTimeoutException

class TimelineViewModel @AssistedInject constructor(
    timelineStoreFactory: TimelineStore.Factory,
    noteStreaming: NoteStreaming,
    accountRepository: AccountRepository,
    loggerFactory: Logger.Factory,
    private val accountStore: AccountStore,
    private val timelineFilterServiceFactory: TimelineFilterService.Factory,
    planeNoteViewDataCacheFactory: PlaneNoteViewDataCache.Factory,
    private val configRepository: LocalConfigRepository,
    private val timelineScrollPositionRepository: TimelineScrollPositionRepository,
    @Assisted val accountId: AccountId?,
    @Assisted val pageId: PageId?,
    @Assisted val pageable: Pageable,
    @Assisted val isSaveScrollPosition: Boolean,
) : ViewModel() {

    @AssistedFactory
    interface ViewModelAssistedFactory {
        fun create(
            accountId: AccountId?,
            pageId: PageId?,
            pageable: Pageable,
            isSaveScrollPosition: Boolean,
        ): TimelineViewModel
    }

    companion object

    val tag = "TimelineViewModel"


    var position: Int = 0
    var offset: Int = 0
    private val currentAccountWatcher = CurrentAccountWatcher(
        if (accountId?.value != null && accountId.value <= 0) null else accountId?.value,
        accountRepository
    )

    val timelineStore: TimelineStore =
        timelineStoreFactory.create(pageable, viewModelScope, currentAccountWatcher::getAccount)

    private val timelineFilterService by lazy {
        timelineFilterServiceFactory.create(pageable)
    }

    private val timelineState = timelineStore.timelineState.map { pageableState ->
        pageableState.suspendConvert { list ->
            cache.useByIds(list).filterNot { note ->
                note.filterResult == PlaneNoteViewData.FilterResult.ShouldFilterNote
            }
        }
    }.stateIn(
        viewModelScope + Dispatchers.Default,
        SharingStarted.WhileSubscribed(5_000),
        PageableState.Loading.Init()
    )

    val timelineListState: StateFlow<List<TimelineListItem>> = timelineState.map { state ->
        state.toList()
    }.stateIn(
        viewModelScope + Dispatchers.Default,
        SharingStarted.Lazily,
        listOf(TimelineListItem.Loading)
    )


    val errorEvent = timelineStore.timelineState.map {
        (it as? PageableState.Error)?.throwable
    }.filterNotNull().shareIn(viewModelScope, SharingStarted.WhileSubscribed())

    val isLoading = timelineStore.timelineState.map {
        it is PageableState.Loading
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        false
    )


    private val logger = loggerFactory.create("TimelineViewModel")
    private val cache = planeNoteViewDataCacheFactory.create(
        currentAccountWatcher::getAccount,
        viewModelScope
    )

    private val noteStreamingCollector = NoteStreamingCollector(
        accountStore = accountStore,
        timelineStore = timelineStore,
        coroutineScope = viewModelScope,
        logger = logger,
        currentAccountWatcher = currentAccountWatcher,
        noteStreaming = noteStreaming,
        pageable = pageable,
    )

    private val pagingCoroutineScope = PagingLoaderScopeController(viewModelScope)

    private var isActive = true

    private val saveScrollPositionScrolledEvent = MutableSharedFlow<Int>(extraBufferCapacity = 4)

    init {

        viewModelScope.launch {
            accountStore.observeCurrentAccount.filterNotNull().distinctUntilChanged().map {
                currentAccountWatcher.getAccount()
            }.distinctUntilChanged().catch {
                logger.error("observe account error", it)
            }.collect {
                loadInit()
            }
        }

        saveScrollPositionScrolledEvent.distinctUntilChanged().throttleLatest(500).onEach {
            saveNowScrollPosition()
        }.launchIn(viewModelScope)

        cache.addFilter(object : PlaneNoteViewDataCache.ViewDataFilter {
            override suspend fun check(viewData: PlaneNoteViewData): PlaneNoteViewData.FilterResult {
                return timelineFilterService.filterNote(viewData).filterResult
            }
        })
        cache.addFilter(object : PlaneNoteViewDataCache.ViewDataFilter {
            override suspend fun check(viewData: PlaneNoteViewData): PlaneNoteViewData.FilterResult {
                if (
                    (pageable as? CanExcludeReplies<*>)?.getExcludeReplies() == true
                    && viewData.toShowNote.note.replyId != null
                    ) {
                    return PlaneNoteViewData.FilterResult.ShouldFilterNote
                }

                if ((pageable as? CanExcludeReposts<*>)?.getExcludeReposts() == true
                    && viewData.toShowNote.note.isRenoteOnly()
                    ) {
                    return PlaneNoteViewData.FilterResult.ShouldFilterNote
                }
                return viewData.filterResult
            }
        })
    }


    fun loadNew() {
        pagingCoroutineScope.launch {
            timelineStore.loadFuture().onFailure {
                logger.error("load future timeline failed", it)
            }
        }
    }

    fun loadOld() {
        pagingCoroutineScope.launch {
            timelineStore.loadPrevious().onFailure {
                logger.error("load previous timeline failed", it)
            }
        }
    }

    fun loadInit(initialUntilDate: Instant? = null, ignoreSavedScrollPosition: Boolean = false) {
        pagingCoroutineScope.cancel()
        pagingCoroutineScope.launch {
            cache.clear()

            if (ignoreSavedScrollPosition) {
                pageId?.let {
                    timelineScrollPositionRepository.remove(it.value)
                }
            }
            val savedScrollPositionId = pageId?.value?.let {
                timelineScrollPositionRepository.get(it)
            }?.takeIf {
                !ignoreSavedScrollPosition
            }

            timelineStore.clear(initialUntilDate?.let {
                InitialLoadQuery.UntilDate(it)
            } ?: savedScrollPositionId?.let {
                InitialLoadQuery.UntilId(it)
            })
            timelineStore.loadPrevious().onFailure {
                logger.error("load initial timeline failed", it)
            }
            timelineStore.loadFuture().onFailure {
                logger.error("load initial timeline failed", it)
            }
        }
    }


    fun onResume() {
        isActive = true
        viewModelScope.launch {
            val config = configRepository.get().getOrNull()
            if (config?.isEnableStreamingAPIAndNoteCapture == false) {
                // NOTE: 自動更新が無効であればNoteのCaptureとStreaming APIを停止している
                timelineStore.suspendStreaming()
                noteStreamingCollector.onSuspend()
                cache.suspendNoteCapture()
            } else {
                // NOTE: 自動更新が有効なのでNote CaptureとStreaming APIを再開している
                noteStreamingCollector.onResume()

                cache.captureNotesBy(
                    (timelineState.value.content as? StateContent.Exist)?.rawContent?.map {
                        it.id
                    } ?: emptyList()
                )
                cache.captureNotes()
            }

            if (config?.isStopStreamingApiWhenBackground == true) {
                loadNew()
            }
        }
    }

    fun onPause() {
        isActive = false
        viewModelScope.launch {
            val config = configRepository.get().getOrNull()
            if (config?.isStopStreamingApiWhenBackground == true) {
                timelineStore.suspendStreaming()
                noteStreamingCollector.onSuspend()
            }

            if (config?.isStopNoteCaptureWhenBackground == true) {
                cache.suspendNoteCapture()
            }

            saveNowScrollPosition()
        }
    }

    fun onScrollPositionChanged(firstVisiblePosition: Int) {
        if (firstVisiblePosition <= 3) {
            onVisibleFirst()
        } else {
            // NOTE: 先頭を表示していない時はストリーミングを停止する
            timelineStore.suspendStreaming()
        }
        viewModelScope.launch {
            try {
                timelineStore.releaseUnusedPages(firstVisiblePosition)
            } catch (e: IllegalArgumentException) {
                logger.log("release unused pages failed")
                logger.error("release unused pages failed", e)
            }
        }
        saveScrollPositionScrolledEvent.tryEmit(firstVisiblePosition)
    }

    private fun onVisibleFirst() {
        viewModelScope.launch {
            if (this@TimelineViewModel.isActive
                && !timelineStore.isActiveStreaming
                && timelineState.value !is PageableState.Loading
            ) {
                timelineStore.loadFuture()
            }
        }
    }

    private suspend fun saveNowScrollPosition() {
        if (isSaveScrollPosition && pageId != null) {
            val listState = timelineListState.value
            var savePos = position - 1
            while(savePos < listState.size && listState.getOrNull(savePos) !is TimelineListItem.Note) {
                savePos++
            }
            val savePosId: Note.Id? = listState.getOrNull(savePos)?.let {
                (it as? TimelineListItem.Note)?.note?.note?.note?.id
            }
            savePosId?.also {
                timelineScrollPositionRepository.save(
                    pageId.value,
                    it
                )
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
fun TimelineViewModel.Companion.provideViewModel(
    assistedFactory: TimelineViewModel.ViewModelAssistedFactory,
    accountId: AccountId?,
    pageId: PageId?,
    pageable: Pageable,
    isSaveScrollPosition: Boolean?,

    ) = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return assistedFactory.create(
            accountId = accountId,
            pageId = pageId,
            pageable,
            isSaveScrollPosition ?: false,
        ) as T
    }
}


class PageId(val value: Long)


class AccountId(val value: Long)

sealed interface TimelineListItem {
    object Loading : TimelineListItem
    data class Note(val note: PlaneNoteViewData) : TimelineListItem
    data class Error(val throwable: Throwable) : TimelineListItem {
        fun getErrorMessage(): StringSource {
            return when (throwable) {
                is SocketTimeoutException -> {
                    StringSource(R.string.timeout_error)
                }
                is IOException -> {
                    StringSource(R.string.timeout_error)
                }
                is APIError -> {
                    APIErrorStringConverter()(throwable)
                }
                is UnauthorizedException -> {
                    StringSource(R.string.unauthorized_error)
                }
                else -> {
                    StringSource("error:$throwable")
                }
            }
        }

        fun isUnauthorizedError(): Boolean {
            return throwable is APIError.AuthenticationException
                    || throwable is APIError.ForbiddenException
                    || throwable is UnauthorizedException
        }
    }

    object Empty : TimelineListItem
}

fun PageableState<List<PlaneNoteViewData>>.toList(): List<TimelineListItem> {
    return when (val content = this.content) {
        is StateContent.Exist -> {
            content.rawContent.map {
                TimelineListItem.Note(it)
            } + if (this is PageableState.Loading.Previous) {
                listOf(TimelineListItem.Loading)
            } else {
                emptyList()
            }
        }
        is StateContent.NotExist -> {
            listOf(
                when (this) {
                    is PageableState.Error -> {
                        TimelineListItem.Error(this.throwable)
                    }
                    is PageableState.Fixed -> {
                        TimelineListItem.Empty
                    }
                    is PageableState.Loading -> TimelineListItem.Loading
                }
            )
        }
    }
}

class NoteStreamingCollector(
    val coroutineScope: CoroutineScope,
    val timelineStore: TimelineStore,
    val accountStore: AccountStore,
    val noteStreaming: NoteStreaming,
    val logger: Logger,
    val pageable: Pageable,
    val currentAccountWatcher: CurrentAccountWatcher,
) {

    private var job: Job? = null

    fun onSuspend() {
        synchronized(this) {
            job?.cancel()
            job = null
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun onResume() {
        synchronized(this) {
            if (job != null) {
                return
            }
            job = accountStore.observeCurrentAccount.filterNotNull().distinctUntilChanged()
                .flatMapLatest {
                    noteStreaming.connect(currentAccountWatcher::getAccount, pageable)
                }.map {
                    timelineStore.onReceiveNote(it.id)
                }.catch {
                    logger.error("receive not error", it)
                }.launchIn(coroutineScope + Dispatchers.IO)
        }

    }
}

class PagingLoaderScopeController(private val coroutineScope: CoroutineScope) {

    private var jobs = listOf<Job>()

    fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        val job = coroutineScope.launch {
            block.invoke(coroutineScope)
        }
        jobs = jobs + job
        return job
    }

    fun cancel() {
        jobs.forEach {
            it.cancel()
        }
        jobs = emptyList()
    }

}