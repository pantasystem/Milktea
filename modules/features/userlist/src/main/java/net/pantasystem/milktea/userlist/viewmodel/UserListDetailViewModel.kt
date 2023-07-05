package net.pantasystem.milktea.userlist.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.list.UserList
import net.pantasystem.milktea.model.list.UserListRepository
import net.pantasystem.milktea.model.list.UserListTabToggleAddToTabUseCase
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import javax.inject.Inject

@HiltViewModel
class UserListDetailViewModel @Inject constructor(
    private val userListRepository: UserListRepository,
    private val userDataSource: UserDataSource,
    accountStore: AccountStore,
    private val toggleAddToTabUseCase: UserListTabToggleAddToTabUseCase,
    loggerFactory: Logger.Factory,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {


    companion object {
        const val EXTRA_LIST_ID = "jp.panta.misskeyandroidclient.EXTRA_LIST_ID"
        const val EXTRA_ADD_TAB_TO_ACCOUNT_ID = "jp.panta.misskeyandroidclient.EXTRA_ADD_TAB_TO_ACCOUNT_ID"
    }

    private val listIdFlow = savedStateHandle.getStateFlow<UserList.Id?>(
        EXTRA_LIST_ID,
        null
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val account = listIdFlow.flatMapLatest { listId ->
        accountStore.state.map {  state ->
            listId?.let {
                state.get(it.accountId)
            } ?: state.currentAccount
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val userList = listIdFlow.filterNotNull().flatMapLatest {
        userListRepository.observeOne(it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)


    @OptIn(ExperimentalCoroutinesApi::class)
    private val addToTabAccount = savedStateHandle.getStateFlow<Long?>(
        EXTRA_ADD_TAB_TO_ACCOUNT_ID,
        null
    ).flatMapLatest { accountId ->
        accountStore.state.map { state ->
            accountId?.let {
                state.get(accountId)
            } ?: state.currentAccount
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val isAddedToTab = combine(account, addToTabAccount,listIdFlow) { account, addTo, listId ->
        (addTo?.pages ?: account?.pages)?.any {
            it.pageParams.listId == listId?.userListId
        } ?: false
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val users = userList.filterNotNull().flatMapLatest {
        userDataSource.observeIn(
            it.userList.id.accountId,
            it.userList.userIds.map { userId -> userId.id })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())


    private val logger = loggerFactory.create("UserListDetailViewModel")

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            runCancellableCatching {
                savedStateHandle.get<UserList.Id?>(EXTRA_LIST_ID)?.let {
                    userListRepository.syncOne(it)
                }
            }.onSuccess {
                logger.info("load list success")
            }.onFailure {
                logger.error("load list error", e = it)
            }

        }

    }

    fun updateName(name: String) {
        viewModelScope.launch {
            runCancellableCatching {
                val listId = savedStateHandle.get<UserList.Id>(EXTRA_LIST_ID)
                    ?: throw IllegalStateException("listId is null")
                userListRepository.update(listId, name)
                userListRepository.syncOne(listId).getOrThrow()
            }.onSuccess {
                load()
            }.onFailure { t ->
                logger.error("名前の更新に失敗した", e = t)
            }
        }

    }

    fun pushUser(userId: User.Id) {

        viewModelScope.launch {
            runCancellableCatching {
                val listId = savedStateHandle.get<UserList.Id>(EXTRA_LIST_ID)
                    ?: throw IllegalStateException("listId is null")
                userListRepository.appendUser(listId, userId)
                userListRepository.syncOne(listId).getOrThrow()
            }.onSuccess {
                logger.info("ユーザーの追加に成功")
            }.onFailure {
                logger.warning("ユーザーの追加に失敗", e = it)
            }
        }

    }


    fun pullUser(userId: User.Id) {

        viewModelScope.launch {
            runCancellableCatching {
                val listId = savedStateHandle.get<UserList.Id>(EXTRA_LIST_ID)
                    ?: throw IllegalStateException("listId is null")
                userListRepository.removeUser(listId, userId)
                userListRepository.syncOne(listId).getOrThrow()
            }.onFailure { t ->
                logger.warning("ユーザーの除去に失敗", e = t)
            }.onSuccess {
                logger.info("ユーザーの除去に成功")
            }
        }


    }

    fun toggleAddToTab() {
        viewModelScope.launch {
            val listId = savedStateHandle.get<UserList.Id>(EXTRA_LIST_ID)
                ?: throw IllegalStateException("listId is null")

            toggleAddToTabUseCase(listId, savedStateHandle[EXTRA_ADD_TAB_TO_ACCOUNT_ID]).onFailure {
                logger.error("Page追加に失敗", it)
            }

        }
    }

    fun getUserListId(): UserList.Id {
        return requireNotNull(savedStateHandle[EXTRA_LIST_ID])
    }

}