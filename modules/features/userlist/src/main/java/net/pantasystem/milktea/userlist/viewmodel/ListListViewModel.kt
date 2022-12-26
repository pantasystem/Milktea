package net.pantasystem.milktea.userlist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.*
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.list.UserList
import net.pantasystem.milktea.model.list.UserListRepository
import net.pantasystem.milktea.model.list.UserListWithMembers
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class ListListViewModel @Inject constructor(
    val accountStore: AccountStore,
    val accountRepository: AccountRepository,
    val loggerFactory: Logger.Factory,
    private val userListRepository: UserListRepository,
    private val userRepository: UserRepository
) : ViewModel() {


    private val userListsFlow =
        accountStore.observeCurrentAccount.filterNotNull().flatMapLatest { account ->
            userListRepository.observeByAccountId(account.accountId)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val userListsSyncState =
        accountStore.observeCurrentAccount.filterNotNull().flatMapLatest {
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
        accountStore.observeCurrentAccount,
        userListsFlow,
        userListsSyncState,
        addTargetUserId
    ) { ac, userLists, syncState, addUser ->
        UserListsUiState(
            userLists.map { userList ->
                UserListBindingModel(
                    userList,
                    ac?.pages?.any {
                        it.pageParams.listId == userList.userList.id.userListId
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
                runCancellableCatching {
                    val account = accountRepository.get(userList.id.accountId).getOrThrow()
                    val exPage = account.pages.firstOrNull {
                        val pageable = it.pageable()
                        if (pageable is Pageable.UserListTimeline) {
                            pageable.listId == ul.id.userListId
                        } else {
                            false
                        }
                    }
                    if (exPage == null) {
                        val page = Page(
                            account.accountId,
                            ul.name,
                            pageable = Pageable.UserListTimeline(
                                ul.id.userListId
                            ),
                            weight = 0
                        )
                        accountStore.addPage(page)
                    } else {
                        accountStore.removePage(exPage)
                    }
                }.onFailure {
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