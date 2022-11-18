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
import net.pantasystem.milktea.data.gettters.NoteRelationGetter
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.notes.*
import net.pantasystem.milktea.model.notes.renote.CreateRenoteMultipleAccountUseCase
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
    val renoteUseCase: CreateRenoteMultipleAccountUseCase,
    val noteRelationGetter: NoteRelationGetter,
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
        _selectedAccountIds,
        note,
    ) { accountWithUser, selectedIds, note ->
        accountWithUser.map { (account, user) ->
            AccountWithUser(
                accountId = account.accountId,
                user = user,
                isSelected = selectedIds.any { id -> id == account.accountId },
                isEnable = note?.canRenote(account, user) == true,
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
            note = n?.note,
            syncState = s
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        RenoteDialogViewModelTargetNoteState(
            _syncState.value,
            note.value?.note,
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

    fun renote() {
        val noteId = _targetNoteId.value
            ?: return
        viewModelScope.launch(Dispatchers.IO) {
            renoteUseCase(noteId, _selectedAccountIds.value).onSuccess {
                _resultEvents.tryEmit(
                    RenoteActionResultEvent.Success(
                        noteId, RenoteActionResultEvent.Type.Renote
                    )
                )
            }.onFailure {
                _resultEvents.tryEmit(
                    RenoteActionResultEvent.Failed(
                        noteId, RenoteActionResultEvent.Type.Renote
                    )
                )
            }
        }
    }


    fun unRenote() {
        val noteId = _targetNoteId.value
            ?: return
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.delete(noteId).onSuccess {
                _resultEvents.tryEmit(
                    RenoteActionResultEvent.Success(
                        noteId, RenoteActionResultEvent.Type.UnRenote
                    )
                )
            }.onFailure { t ->
                _resultEvents.tryEmit(
                    RenoteActionResultEvent.Failed(
                        noteId, RenoteActionResultEvent.Type.UnRenote
                    )
                )
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
    val isEnable: Boolean,
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

private fun NoteRelation.canRenote(account: Account, user: User): Boolean {
    if (note.canRenote(user.id)) {
        return true
    }


    // NOTE: 公開範囲がpublicなら可能
    if (note.visibility is Visibility.Public && !note.visibility.isLocalOnly()) {
        return true
    }

    // NOTE: 公開範囲がフォロワー、DMの場合は無理
    if (note.visibility is Visibility.Followers || note.visibility is Visibility.Specified) {
        return false
    }

    // NOTE: 同一ホストなら可能
    if (note.visibility.isLocalOnly() && this.user.host == account.getHost()) {
        return true
    }

    return false

}