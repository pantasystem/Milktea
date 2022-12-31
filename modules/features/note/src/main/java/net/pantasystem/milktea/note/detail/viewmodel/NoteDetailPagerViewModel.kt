package net.pantasystem.milktea.note.detail.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.notes.InitialLoadQuery
import net.pantasystem.milktea.app_store.notes.TimelineStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.CurrentAccountWatcher
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteStreaming
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NoteDetailPagerViewModel @Inject constructor(
    timelineStoreFactory: TimelineStore.Factory,
    accountRepository: AccountRepository,
    noteStreaming: NoteStreaming,
    accountStore: AccountStore,
    loggerFactory: Logger.Factory,
//    cacheFactory: PlaneNoteViewDataCache.Factory
    savedStateHandle: SavedStateHandle,
): ViewModel() {

    companion object {
        const val EXTRA_FROM_PAGEABLE = "NoteDetailPagerViewModel.EXTRA_FROM_PAGEABLE"
        const val EXTRA_NOTE_ID = "NoteDetailPagerViewModel.EXTRA_NOTE_ID"
        const val EXTRA_ACCOUNT_ID = "NoteDetailPagerViewModel.EXTRA_ACCOUNT_ID"
    }

    private val logger by lazy {
        loggerFactory.create("NoteDetailPagerVM")
    }

    private val noteId by lazy {
        requireNotNull(savedStateHandle.get<String>(EXTRA_NOTE_ID))
    }
    private val pageable by lazy {
        savedStateHandle.get<Pageable>(EXTRA_FROM_PAGEABLE) ?: Pageable.Show(noteId)
    }

    private val accountWatcher = CurrentAccountWatcher(savedStateHandle[EXTRA_ACCOUNT_ID], accountRepository)
    private val timelineStore = timelineStoreFactory.create(pageable, viewModelScope) {
        accountWatcher.getAccount()
    }

    val noteIds = combine(timelineStore.timelineState, flowOf(noteId)) { state, noteId ->
        when(val content = state.content) {
            is StateContent.Exist -> listOf(Note.Id(accountWatcher.getAccount().accountId, noteId)) + content.rawContent
            is StateContent.NotExist -> listOf(Note.Id(accountWatcher.getAccount().accountId, noteId))
        }.distinct().sortedBy {
            it.noteId
        }.reversed()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        load()

        accountStore.observeCurrentAccount.filterNotNull().distinctUntilChanged().flatMapLatest {
            noteStreaming.connect(accountWatcher::getAccount, pageable)
        }.map {
            timelineStore.onReceiveNote(it.id)
        }.catch {
            logger.error("receive not error", it)
        }.launchIn(viewModelScope + Dispatchers.IO)
    }

    fun load() {
        viewModelScope.launch {
            runCatching {
                timelineStore.clear(InitialLoadQuery.UntilId(
                    Note.Id(accountWatcher.getAccount().accountId, noteId)
                ))
                timelineStore.loadPrevious()
                timelineStore.loadFuture()
            }

        }
    }
    fun loadPrevious() {
        viewModelScope.launch {
            timelineStore.loadPrevious()
        }
    }

    fun loadFuture() {
        viewModelScope.launch {
            timelineStore.loadFuture()
        }
    }
}
