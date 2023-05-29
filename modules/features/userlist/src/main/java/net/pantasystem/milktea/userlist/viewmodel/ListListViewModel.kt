package net.pantasystem.milktea.userlist.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.*
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.list.UserList
import net.pantasystem.milktea.model.list.UserListRepository
import net.pantasystem.milktea.model.list.UserListTabToggleAddToTabUseCase
import net.pantasystem.milktea.model.list.UserListWithMembers
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject

@HiltViewModel
class ListListViewModel @Inject constructor(
    val accountStore: AccountStore,
    val accountRepository: AccountRepository,
    val loggerFactory: Logger.Factory,
    private val userListRepository: UserListRepository,
    private val userRepository: UserRepository,
    private val toggleAddToTabUseCase: UserListTabToggleAddToTabUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    companion object {
        const val EXTRA_SPECIFIED_ACCOUNT_ID = "ListListViewModel.EXTRA_SPECIFIED_ACCOUNT_ID"
        const val EXTRA_ADD_TAB_TO_ACCOUNT_ID = "ListListViewModel.EXTRA_ADD_TAB_TO_ACCOUNT_ID"
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    private val currentAccount = savedStateHandle.getStateFlow<Long?>(
        EXTRA_SPECIFIED_ACCOUNT_ID,
        null
    ).flatMapLatest { accountId ->
        accountStore.state.map { state ->
            accountId?.let {
                state.get(it)
                    ?: state.currentAccount
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val addTabToAccount = savedStateHandle.getStateFlow<Long?>(
        EXTRA_ADD_TAB_TO_ACCOUNT_ID,
        null
    ).flatMapLatest { accountId ->
        accountStore.state.map { state ->
            accountId?.let { accountId ->
                state.get(accountId)
            } ?: state.currentAccount
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val userListsFlow =
        currentAccount.filterNotNull().flatMapLatest { account ->
            userListRepository.observeByAccountId(account.accountId)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val userListsSyncState =
        currentAccount.filterNotNull().flatMapLatest {
            suspend {
                userListRepository.syncByAccountId(it.accountId).getOrThrow()
            }.asLoadingStateFlow()
        }.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5_000), ResultState.Loading(
                StateContent.NotExist()
            )
        )

    private val addTargetUserId = MutableStateFlow<User.Id?>(null)

    val uiState = combine(
        currentAccount,
        addTabToAccount,
        userListsFlow,
        userListsSyncState,
        addTargetUserId
    ) { ac, addTabToAccount, userLists, syncState, addUser ->
        UserListsUiState(
            userLists.map { userList ->
                UserListBindingModel(
                    userList,
                    addTabToAccount?.pages?.any {
                        it.pageParams.listId == userList.userList.id.userListId
                                && userList.userList.id.accountId == (it.attachedAccountId ?: it.accountId)
                    } ?: false,
                    isTargetUserAdded = userList.userList.userIds.any { id ->
                        id == addUser
                    },
                )
            },
            syncState = syncState,
            addTargetUserId = addUser,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        UserListsUiState(emptyList(), userListsSyncState.value)
    )



    private val logger = loggerFactory.create("ListListViewModel")

    init {
        viewModelScope.launch {
            accountStore.observeCurrentAccount.distinctUntilChanged().filterNotNull().map {
                syncUsers(it.accountId)
            }.catch {
                logger.error("sync users error", it)
            }.collect()
        }
    }

    fun toggle(userList: UserList, userId: User.Id) {
        viewModelScope.launch {
            runCancellableCatching {
                if (userList.userIds.contains(userId)) {
                    userListRepository.removeUser(userList.id, userId)
                } else {
                    userListRepository.appendUser(userList.id, userId)
                }
                userListRepository.syncOne(userList.id)
            }.onFailure {
                logger.error("toggle user failed", it)
            }
        }
    }

    fun toggleTab(userList: UserList?) {
        userList?.let { ul ->
            viewModelScope.launch {
                toggleAddToTabUseCase(ul.id, savedStateHandle[EXTRA_ADD_TAB_TO_ACCOUNT_ID]).onFailure {
                    logger.error("タブtoggle処理失敗", e = it)
                }
            }
        }
    }

    fun createUserList(name: String) {
        viewModelScope.launch {
            runCancellableCatching {
                val account = accountRepository.getCurrentAccount().getOrThrow()
                val result = userListRepository.create(account.accountId, name)
                userListRepository.syncOne(result.id).getOrThrow()
            }.onSuccess {
                logger.debug("作成成功")
            }.onFailure {
                logger.error("作成失敗", it)
            }
        }

    }

    fun setAddTargetUserId(userId: User.Id?) {
        addTargetUserId.value = userId
    }

    private suspend fun syncUsers(accountId: Long) {
        val userIds = userListRepository.findByAccountId(accountId).map {
            it.userIds
        }.flatten().distinct()
        userRepository.syncIn(userIds)
    }

}

data class UserListBindingModel(
    val userList: UserListWithMembers,
    val isAddedTab: Boolean,
    val isTargetUserAdded: Boolean,
)

data class UserListsUiState(
    val userLists: List<UserListBindingModel>,
    val syncState: ResultState<Unit>,
    val addTargetUserId: User.Id? = null,
)