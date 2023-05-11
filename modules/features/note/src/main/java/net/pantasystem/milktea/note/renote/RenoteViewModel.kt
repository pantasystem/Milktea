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
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.asLoadingStateFlow
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.instance.InstanceInfoService
import net.pantasystem.milktea.model.instance.InstanceInfoType
import net.pantasystem.milktea.model.notes.*
import net.pantasystem.milktea.model.notes.repost.CreateRenoteMultipleAccountUseCase
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
    val instanceInfoService: InstanceInfoService,
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
                isEnable = note?.contentNote?.canRenote(account, user) == true,
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
        RenoteViewModelTargetNoteState(
            note = n?.contentNote?.note,
            syncState = s
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        RenoteViewModelTargetNoteState(
            _syncState.value,
            note.value?.contentNote?.note,
        )
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val currentAccountInstanceInfo = accountStore.observeCurrentAccount.filterNotNull().flatMapLatest {
        instanceInfoService.observe(it.normalizedInstanceUri)
    }.catch {
        logger.error("observe current account error", it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val uiState = combine(
        _targetNoteId,
        _noteState,
        accountWithUsers,
        currentAccountInstanceInfo,
    ) { noteId, syncState, accounts, instanceInfo ->
        RenoteViewModelUiState(
            targetNoteId = noteId,
            noteState = syncState,
            accounts = accounts,
            canQuote = instanceInfo is InstanceInfoType.Misskey
                    || (instanceInfo as? InstanceInfoType.Mastodon)?.info?.featureQuote == true
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        RenoteViewModelUiState(
            _targetNoteId.value,
            _noteState.value,
            accountWithUsers.value,
            true
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

data class RenoteViewModelUiState(
    val targetNoteId: Note.Id?,
    val noteState: RenoteViewModelTargetNoteState,
    val accounts: List<AccountWithUser>,
    val canQuote: Boolean,
)

data class RenoteViewModelTargetNoteState(
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
    data class Renote(
        val result: Result<List<Result<Note>>>,
        val noteId: Note.Id,
        val accounts: List<Long>
    ) : RenoteActionResultEvent

    data class UnRenote(val result: Result<Note>, val noteId: Note.Id) : RenoteActionResultEvent
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

    // NOTE: 投稿のホストとアカウントが同一ホストかつ公開範囲の場合はRenote可能
    if (note.visibility is Visibility.Home && this.user.host == account.getHost()) {
        return true
    }

    return false
}