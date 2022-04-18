package jp.panta.misskeyandroidclient.ui.list.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.model.account.AccountStore
import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.list.UserList
import net.pantasystem.milktea.model.list.UserListStore
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class ListListViewModel @Inject constructor(
    val miCore: MiCore,
    val encryption: Encryption,
    private val userListStore: net.pantasystem.milktea.model.list.UserListStore,
    val accountStore: net.pantasystem.milktea.model.account.AccountStore
) : ViewModel() {


    val userListList = accountStore.observeCurrentAccount.filterNotNull().flatMapLatest { account ->
        userListStore.state.map {
            it.getUserLists(account.accountId)
        }
    }.asLiveData()

    val pagedUserList =
        accountStore.observeCurrentAccount.filterNotNull().flatMapLatest { account ->
            userListStore.state.map {
                it.getUserLists(account.accountId)
            }.map { lists ->
                lists.filter { list ->
                    account.pages.any { page ->
                        list.id.userListId == page.pageParams.listId
                    }
                }.toSet()
            }

        }.asLiveData()

    val showUserDetailEvent = EventBus<net.pantasystem.milktea.model.list.UserList>()

    private val logger = miCore.loggerFactory.create("ListListViewModel")

    init {
        accountStore.observeCurrentAccount.onEach {
            fetch()
        }.launchIn(viewModelScope + Dispatchers.IO)
    }


    fun fetch() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val account = miCore.getAccountRepository().getCurrentAccount()
                loadListList(account.accountId)
            }.onSuccess {
                logger.debug("success fetch")
            }.onFailure {
                logger.error("fetch error", e = it)
            }

        }
    }


    private suspend fun loadListList(accountId: Long): List<net.pantasystem.milktea.model.list.UserList> {
        return userListStore.findByAccount(accountId)
    }


    fun showUserListDetail(userList: net.pantasystem.milktea.model.list.UserList?) {
        userList?.let { ul ->
            showUserDetailEvent.event = ul
        }
    }

    fun toggleTab(userList: net.pantasystem.milktea.model.list.UserList?) {
        userList?.let { ul ->
            viewModelScope.launch(Dispatchers.IO) {
                runCatching {
                    val account = miCore.getAccountRepository().get(userList.id.accountId)
                    val exPage = account.pages.firstOrNull {
                        val pageable = it.pageable()
                        if (pageable is net.pantasystem.milktea.model.account.page.Pageable.UserListTimeline) {
                            pageable.listId == ul.id.userListId
                        } else {
                            false
                        }
                    }
                    if (exPage == null) {
                        val page = net.pantasystem.milktea.model.account.page.Page(
                            account.accountId,
                            ul.name,
                            pageable = net.pantasystem.milktea.model.account.page.Pageable.UserListTimeline(
                                ul.id.userListId
                            ),
                            weight = 0
                        )
                        miCore.getAccountStore().addPage(page)
                    } else {
                        miCore.getAccountStore().removePage(exPage)
                    }
                }.onFailure {
                    logger.error("タブtoggle処理失敗", e = it)
                }
            }
        }
    }

    fun delete(userList: net.pantasystem.milktea.model.list.UserList?) {
        userList ?: return
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                userListStore.delete(userList.id)
            }.onSuccess {
                logger.debug("削除成功")
            }
        }

    }

    fun createUserList(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val account = miCore.getAccountRepository().getCurrentAccount()
                userListStore.create(account.accountId, name)
            }.onSuccess {
                logger.debug("作成成功")
            }
        }

    }


}