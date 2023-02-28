package net.pantasystem.milktea.userlist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.list.UserList
import net.pantasystem.milktea.model.list.UserListRepository
import net.pantasystem.milktea.model.list.UserListTabToggleAddToTabUseCase
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource

class UserListDetailViewModel @AssistedInject constructor(
    private val userListRepository: UserListRepository,
    private val userDataSource: UserDataSource,
    accountStore: AccountStore,
    private val accountRepository: AccountRepository,
    private val toggleAddToTabUseCase: UserListTabToggleAddToTabUseCase,
    loggerFactory: Logger.Factory,
    @Assisted val listId: UserList.Id,
) : ViewModel() {

    @AssistedFactory
    interface ViewModelAssistedFactory {
        fun create(listId: UserList.Id): UserListDetailViewModel
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            assistedFactory: ViewModelAssistedFactory,
            listId: UserList.Id
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(listId) as T
            }
        }
    }


    val userList = userListRepository.observeOne(listId).filterNotNull().map {
        it.userList
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)


    val isAddedToTab = accountStore.observeAccounts.mapNotNull {
        it.firstOrNull { ac ->
            ac.accountId == listId.accountId
        }
    }.map { account ->
        account.pages.firstOrNull {
            (it.pageable() as? Pageable.UserListTimeline)?.listId == listId.userListId
        }
    }.map { page ->
        page != null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val users = userListRepository.observeOne(listId).filterNotNull().flatMapLatest {
        userDataSource.observeIn(listId.accountId, it.userList.userIds.map { userId -> userId.id })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(FlowPreview::class)
    val account = suspend {
        accountRepository.get(listId.accountId).getOrNull()
    }.asFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val logger = loggerFactory.create("UserListDetailViewModel")

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            runCancellableCatching {
                userListRepository.syncOne(listId)
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
            toggleAddToTabUseCase(listId).onFailure {
                logger.error("Page追加に失敗", it)
            }

        }
    }


}