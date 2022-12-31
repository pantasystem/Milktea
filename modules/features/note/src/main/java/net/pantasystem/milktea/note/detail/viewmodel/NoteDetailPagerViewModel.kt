package net.pantasystem.milktea.note.detail.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.notes.InitialLoadQuery
import net.pantasystem.milktea.app_store.notes.TimelineStore
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.CurrentAccountWatcher
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.notes.Note
import javax.inject.Inject

@HiltViewModel
class NoteDetailPagerViewModel @Inject constructor(
    timelineStoreFactory: TimelineStore.Factory,
    accountRepository: AccountRepository,
    savedStateHandle: SavedStateHandle,
): ViewModel() {

    companion object {
        const val EXTRA_FROM_PAGEABLE = "NoteDetailPagerViewModel.EXTRA_FROM_PAGEABLE"
        const val EXTRA_NOTE_ID = "NoteDetailPagerViewModel.EXTRA_NOTE_ID"
        const val EXTRA_ACCOUNT_ID = "NoteDetailPagerViewModel.EXTRA_ACCOUNT_ID"
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
