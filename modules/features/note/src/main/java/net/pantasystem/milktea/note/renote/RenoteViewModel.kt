package net.pantasystem.milktea.note.renote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.asLoadingStateFlow
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteRepository
import net.pantasystem.milktea.model.notes.renote.CreateRenoteUseCase
import net.pantasystem.milktea.model.user.User
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
    val renoteUseCase: CreateRenoteUseCase,
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
        noteRepository.observeOne(it)
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
    private val accountWithUser = accounts.map { accounts ->
        accounts.map {
            it to User.Id(
                it.accountId,
                it.remoteId
            )
        }.map { (account, userId) ->
            userDataSource.observe(userId).map { user ->
                account to user
            }
        }
    }.flatMapLatest { flows ->
        combine(flows) { users ->
            users.toList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val accountWithUsers = combine(
        accountWithUser,
        _selectedAccountIds
    ) { accountWithUser, selectedIds ->
        accountWithUser.map { (account, user) ->
            AccountWithUser(
                accountId = account.accountId,
                user = user,
                isSelected = selectedIds.any { id -> id == account.accountId }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _syncState = _targetNoteId.filterNotNull().flatMapLatest {
        suspend {
            noteRepository.sync(it).getOrThrow()
        }.asLoadingStateFlow()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ResultState.Loading(StateContent.NotExist())
    )

    private val _noteState = combine(note, _syncState) { n, s ->
        RenoteDialogViewModelTargetNoteState(
            note = n,
            syncState = s
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        RenoteDialogViewModelTargetNoteState(
            _syncState.value,
            note.value,
        )
    )

    val uiState = combine(
        _targetNoteId,
        _noteState,
        accountWithUsers
    ) { noteId, syncState, accounts ->
        RenoteDialogViewModelUiState(
            targetNoteId = noteId,
            noteState = syncState,
            accounts = accounts,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        RenoteDialogViewModelUiState(
            _targetNoteId.value,
            _noteState.value,
            accountWithUsers.value,
        )
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
            } else {
                accounts + accountId
            }
        }
    }

    fun renote(noteId: Note.Id) {
        viewModelScope.launch(Dispatchers.IO) {
            renoteUseCase(noteId).onSuccess {
                _resultEvents.tryEmit(RenoteActionResultEvent.Success(
                    it.id, RenoteActionResultEvent.Type.Renote
                ))
            }.onFailure {
                _resultEvents.tryEmit(RenoteActionResultEvent.Failed(
                    noteId, RenoteActionResultEvent.Type.Renote
                ))
            }
        }
    }


    fun unRenote(noteId: Note.Id) {
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.delete(noteId).onSuccess {
                _resultEvents.tryEmit(RenoteActionResultEvent.Success(
                    noteId, RenoteActionResultEvent.Type.UnRenote
                ))
            }.onFailure { t ->
                _resultEvents.tryEmit(RenoteActionResultEvent.Failed(
                    noteId, RenoteActionResultEvent.Type.UnRenote
                ))
            }
        }
    }


}

data class RenoteDialogViewModelUiState(
    val targetNoteId: Note.Id?,
    val noteState: RenoteDialogViewModelTargetNoteState,
    val accounts: List<AccountWithUser>,
)

data class RenoteDialogViewModelTargetNoteState(
    val syncState: ResultState<Unit>,
    val note: Note?,
)

data class AccountWithUser(
    val accountId: Long,
    val user: User,
    val isSelected: Boolean,
)

sealed interface RenoteActionResultEvent {
    val noteId: Note.Id
    val type: Type

    data class Success(
        override val noteId: Note.Id,
        override val type: Type
    ) : RenoteActionResultEvent

    data class Failed(
        override val noteId: Note.Id,
        override val type: Type
    ) : RenoteActionResultEvent

    enum class Type {
        Renote, UnRenote
    }
}