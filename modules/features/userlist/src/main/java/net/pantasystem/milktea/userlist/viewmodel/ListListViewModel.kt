package net.pantasystem.milktea.userlist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common_android.eventbus.EventBus
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.list.UserList
import net.pantasystem.milktea.model.list.UserListRepository
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class ListListViewModel @Inject constructor(
    val encryption: Encryption,
    val accountStore: AccountStore,
    val accountRepository: AccountRepository,
    val loggerFactory: Logger.Factory,
    private val userListRepository: UserListRepository,
) : ViewModel() {


    val userListList = accountStore.observeCurrentAccount.filterNotNull().flatMapLatest { account ->
        userListRepository.observeByAccountId(account.accountId).map { list ->
            list.map {
                it.userList
            }
        }
    }.asLiveData()

    val pagedUserList =
        accountStore.observeCurrentAccount.filterNotNull().flatMapLatest { account ->
            userListRepository.observeByAccountId(account.accountId).map { lists ->
                lists.filter { list ->
                    account.pages.any { page ->
                        list.userList.id.userListId == page.pageParams.listId
                    }
                }.map {
                    it.userList
                }.toSet()
            }
        }.asLiveData()

    val showUserDetailEvent = EventBus<UserList>()

    private val logger = loggerFactory.create("ListListViewModel")

    init {
        accountStore.observeCurrentAccount.onEach {
            fetch()
        }.launchIn(viewModelScope + Dispatchers.IO)
    }


    fun fetch() {
        viewModelScope.launch(Dispatchers.IO) {
            accountRepository.getCurrentAccount().mapCatching { account ->
                userListRepository.syncByAccountId(account.accountId).getOrThrow()
            }.onSuccess {
                logger.debug("success fetch")
            }.onFailure {
                logger.error("fetch error", e = it)
            }

        }
    }


    fun showUserListDetail(userList: UserList?) {
        userList?.let { ul ->
            showUserDetailEvent.event = ul
        }
    }

    fun toggleTab(userList: UserList?) {
        userList?.let { ul ->
            viewModelScope.launch(Dispatchers.IO) {
                runCatching {
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

    fun delete(userList: UserList?) {
        userList ?: return
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                userListRepository.delete(userList.id)
            }.onSuccess {
                logger.debug("削除成功")
            }
        }

    }

    fun createUserList(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val account = accountRepository.getCurrentAccount().getOrThrow()
                val result = userListRepository.create(account.accountId, name)
                userListRepository.syncOne(result.id).getOrThrow()
            }.onSuccess {
                logger.debug("作成成功")
            }
        }

    }


}