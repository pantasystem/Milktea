package net.pantasystem.milktea.note.detail.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.notes.InitialLoadQuery
import net.pantasystem.milktea.app_store.notes.TimelineStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common_android_ui.account.viewmodel.AccountViewModelUiStateHelper
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.ap.ApResolverService
import net.pantasystem.milktea.model.instance.InstanceInfoService
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject

@HiltViewModel
class NoteDetailPagerViewModel @Inject constructor(
    timelineStoreFactory: TimelineStore.Factory,
    private val savedStateHandle: SavedStateHandle,
    private val apResolverService: ApResolverService,
    private val accountStore: AccountStore,
    private val accountRepository: AccountRepository,
    instanceInfoService: InstanceInfoService,
    userRepository: UserRepository,
    loggerFactory: Logger.Factory,
) : ViewModel() {

    companion object {
        const val EXTRA_FROM_PAGEABLE = "NoteDetailPagerViewModel.EXTRA_FROM_PAGEABLE"
        const val EXTRA_NOTE_ID = "NoteDetailPagerViewModel.EXTRA_NOTE_ID"
        const val EXTRA_ACCOUNT_ID = "NoteDetailPagerViewModel.EXTRA_ACCOUNT_ID"
    }

    private val logger by lazy {
        loggerFactory.create("NoteDetailPagerViewModel")
    }

    private val noteIdStr = savedStateHandle.getStateFlow<String?>(EXTRA_NOTE_ID, null)


    @OptIn(ExperimentalCoroutinesApi::class)
    private val currentAccount = savedStateHandle.getStateFlow<Long?>(
        EXTRA_ACCOUNT_ID,
        null
    ).flatMapLatest { accountId ->
        accountStore.getOrCurrent(accountId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)


    private val timelineStoreHolder = TimelineStoreHolder(
        savedStateHandle.get<Pageable>(EXTRA_FROM_PAGEABLE) ?: Pageable.Show(getNoteId()),
        timelineStoreFactory,
        viewModelScope,
    ) {
        (savedStateHandle[EXTRA_ACCOUNT_ID]
            ?: accountStore.state.value.currentAccount?.accountId)?.let {
            accountRepository.get(it).getOrThrow()
        } ?: throw IllegalStateException("Account is not set")
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    private val timelineState = savedStateHandle.getStateFlow<Pageable?>(EXTRA_FROM_PAGEABLE, null).flatMapLatest {
        if (it != null) {
            timelineStoreHolder.setPageable(it)
        }
        timelineStoreHolder.timelineStore.timelineState
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        PageableState.Loading.Init(),
    )
    val noteIds = combine(
        timelineState,
        noteIdStr.filterNotNull(),
        currentAccount.filterNotNull()
    ) { state, noteId, account ->
        when (val content = state.content) {
            is StateContent.Exist -> listOf(Note.Id(account.accountId, noteId)) + content.rawContent
            is StateContent.NotExist -> listOf(Note.Id(account.accountId, noteId))
        }.distinct().sortedBy {
            it.noteId
        }.reversed()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val accountUiState = AccountViewModelUiStateHelper(
        currentAccount,
        accountStore,
        userRepository,
        instanceInfoService,
        viewModelScope,
    ).uiState

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            runCatching {
                timelineStoreHolder.timelineStore.clear(
                    InitialLoadQuery.UntilId(
                        Note.Id(getAccount().accountId, getNoteId())
                    )
                )
                timelineStoreHolder.timelineStore.loadPrevious()
                timelineStoreHolder.timelineStore.loadFuture()
            }

        }
    }

    fun loadPrevious() {
        viewModelScope.launch {
            timelineStoreHolder.timelineStore.loadPrevious()
        }
    }

    fun loadFuture() {
        viewModelScope.launch {
            timelineStoreHolder.timelineStore.loadFuture()
        }
    }

    fun setCurrentAccount(accountId: Long) {
        viewModelScope.launch {
            try {
                val noteId = getNoteId()
                val currentAccountId = requireNotNull(savedStateHandle.get<Long>(EXTRA_ACCOUNT_ID))
                val resolvedNote =
                    apResolverService.resolve(Note.Id(currentAccountId, noteId), accountId)
                        .getOrThrow()
                savedStateHandle[EXTRA_ACCOUNT_ID] = accountId
                savedStateHandle[EXTRA_NOTE_ID] = resolvedNote.id.noteId
                savedStateHandle[EXTRA_FROM_PAGEABLE] = Pageable.Show(resolvedNote.id.noteId)
                timelineStoreHolder.setPageable(Pageable.Show(resolvedNote.id.noteId))
                accountStore.setCurrent(accountRepository.get(accountId).getOrThrow())
            } catch (e: Exception) {
                logger.error("Failed to resolve note", e)
            }
        }
    }

    private fun getNoteId(): String {
        return requireNotNull(savedStateHandle.get<String>(EXTRA_NOTE_ID))
    }

    private suspend fun getAccount(): Account {
        return (savedStateHandle[EXTRA_ACCOUNT_ID]
            ?: accountStore.state.value.currentAccount?.accountId)?.let {
            accountRepository.get(it).getOrThrow()
        } ?: throw IllegalStateException("Account is not set")
    }
}

class TimelineStoreHolder(
    pageable: Pageable,
    private val timelineStoreFactory: TimelineStore.Factory,
    private val scope: CoroutineScope,
    private val getAccount: suspend () -> Account,
) {
    var timelineStore = timelineStoreFactory.create(pageable, scope) {
        getAccount()
    }
        private set

    fun setPageable(pageable: Pageable) {
        timelineStore = timelineStoreFactory.create(pageable, scope, getAccount = getAccount)
    }
}
