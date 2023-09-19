package net.pantasystem.milktea.note.renote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.asLoadingStateFlow
import net.pantasystem.milktea.common.initialState
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.instance.InstanceInfoService
import net.pantasystem.milktea.model.note.*
import net.pantasystem.milktea.model.note.repost.CreateRenoteMultipleAccountUseCase
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject

@HiltViewModel
class RenoteViewModel @Inject constructor(
    val noteRepository: NoteRepository,
    val accountRepository: AccountRepository,
    val userRepository: UserRepository,
    val accountStore: AccountStore,
    val userDataSource: UserDataSource,
    val renoteUseCase: CreateRenoteMultipleAccountUseCase,
    val noteRelationGetter: NoteRelationGetter,
    private val instanceInfoService: InstanceInfoService,
    renoteUiStateBuilder: RenoteUiStateBuilder,
    loggerFactory: Logger.Factory
) : ViewModel() {

    val logger = loggerFactory.create("RenoteDialogViewModel")

    private var _targetNoteId = MutableStateFlow<Note.Id?>(null)


    private val _resultEvents = MutableSharedFlow<RenoteActionResultEvent>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 20
    )

    val resultEvents = _resultEvents.asSharedFlow()


    @OptIn(ExperimentalCoroutinesApi::class)
    val note = _targetNoteId.filterNotNull().flatMapLatest {
        noteRepository.observeOne(it).filterNotNull().map { note ->
            noteRelationGetter.get(note).getOrNull()
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null
    )

    private val _selectedAccountIds = MutableStateFlow<List<Long>>(emptyList())
    private val accounts = accountStore.observeAccounts.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )


    @OptIn(ExperimentalCoroutinesApi::class)
    private val _syncState = _targetNoteId.filterNotNull().flatMapLatest {
        suspend {
            noteRepository.sync(it).getOrThrow()
        }.asLoadingStateFlow()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ResultState.initialState(),
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val currentAccountInstanceInfo = _targetNoteId.filterNotNull().map {
        accountRepository.get(it.accountId).getOrThrow()
    }.flatMapLatest {
        instanceInfoService.observe(it.normalizedInstanceUri)
    }.catch {
        logger.error("observe current account error", it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)


    val uiState = renoteUiStateBuilder.buildState(
        targetNoteIdFlow = _targetNoteId,
        noteFlow = note,
        noteSyncState = _syncState,
        selectedAccountIds = _selectedAccountIds,
        accountsFlow = accounts,
        currentAccountInstanceInfo = currentAccountInstanceInfo,
        coroutineScope = viewModelScope
    ).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        RenoteViewModelUiState()
    )

    fun setTargetNoteId(noteId: Note.Id) {
        _targetNoteId.value = noteId
        _selectedAccountIds.value = listOf(noteId.accountId)
    }

    fun toggleAddAccount(accountId: Long) {
        _selectedAccountIds.update { accounts ->
            val isExists = accounts.any {
                it == accountId
            }
            if (isExists) {
                accounts - accountId

                // NOTE: 三件以上同時にRenoteするのは流石に不味そうなので三件以上同時にRenoteできないようにする。
            } else if ((accounts.size + 1) < 3) {
                accounts + accountId
            } else {
                accounts
            }
        }
    }

    fun renote() {
        val noteId = _targetNoteId.value
            ?: return
        val accountIds = _selectedAccountIds.value
        viewModelScope.launch {
            val result = renoteUseCase(noteId, accountIds)
            _resultEvents.tryEmit(
                RenoteActionResultEvent.Renote(result, noteId, accountIds)
            )
        }
    }


    fun unRenote() {
        val noteId = _targetNoteId.value
            ?: return
        viewModelScope.launch {
            val result = noteRepository.delete(noteId)
            _resultEvents.tryEmit(
                RenoteActionResultEvent.UnRenote(result, noteId = noteId)
            )
        }
    }


}


sealed interface RenoteActionResultEvent {
    data class Renote(
        val result: Result<List<Result<Note>>>,
        val noteId: Note.Id,
        val accounts: List<Long>
    ) : RenoteActionResultEvent

    data class UnRenote(val result: Result<Note>, val noteId: Note.Id) : RenoteActionResultEvent
}

