package net.pantasystem.milktea.common_android_ui.account.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common_android.eventbus.EventBus
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.SignOutUseCase
import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.instance.Meta
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.model.instance.SyncMetaExecutor
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("UNCHECKED_CAST")
@HiltViewModel
class AccountViewModel @Inject constructor(
    private val accountStore: AccountStore,
    private val userDataSource: UserDataSource,
    loggerFactory: Logger.Factory,
    private val userRepository: UserRepository,
    private val metaRepository: MetaRepository,
    private val signOutUseCase: SignOutUseCase,
    private val syncMetaExecutor: SyncMetaExecutor,
) : ViewModel() {


    private val logger = loggerFactory.create("AccountViewModel")


    private val users = accountStore.observeAccounts.flatMapLatest { accounts ->
        val flows = accounts.map {
            userDataSource.observe(User.Id(it.accountId, it.remoteId)).flowOn(Dispatchers.IO)
        }
        combine(flows) {
            it.toList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val metaList = accountStore.observeAccounts.flatMapLatest { accounts ->
        val flows = accounts.map {
            metaRepository.observe(it.normalizedInstanceDomain).flowOn(Dispatchers.IO)
        }
        combine(flows) {
            it.toList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val accountWithUserList = combine(
        accountStore.observeAccounts,
        users,
        accountStore.observeCurrentAccount,
        metaList,
    ) { accounts, users, current, metaList ->
        val userMap = users.associateBy {
            it.id.accountId
        }
        val metaMap = metaList.filterNotNull().associateBy {
            it.uri
        }
        accounts.map {
            AccountInfo(
                it,
                userMap[it.accountId],
                metaMap[it.normalizedInstanceDomain],
                current?.accountId == it.accountId
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState = combine(
        accountStore.observeCurrentAccount,
        accountWithUserList
    ) { current, accounts ->
        AccountViewModelUiState(
            currentAccount = current,
            accounts = accounts
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccountViewModelUiState())

    val currentAccount =
        accountStore.observeCurrentAccount.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val user = currentAccount.filterNotNull().flatMapLatest { account ->
        userDataSource.observe(User.Id(account.accountId, account.remoteId)).map {
            it as? User.Detail
        }
    }.flowOn(Dispatchers.IO).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val switchAccount = EventBus<Int>()


    val showFollowers = EventBus<User.Id>()
    val showFollowings = EventBus<User.Id>()

    val showProfile = EventBus<Account>()


    init {
        accountStore.observeCurrentAccount.filterNotNull().onEach { ac ->
            userRepository
                .sync(User.Id(ac.accountId, ac.remoteId))
        }.catch { e ->
            logger.error("現在のアカウントの取得に失敗した", e = e)
        }.launchIn(viewModelScope + Dispatchers.IO)

    }

    fun setSwitchTargetConnectionInstance(account: Account) {
        viewModelScope.launch {
            accountStore.setCurrent(account)
            syncMetaExecutor(account.normalizedInstanceDomain)
        }
    }

    fun showSwitchDialog() {
        switchAccount.event = switchAccount.event
    }

    fun showFollowers(userId: User.Id?) {
        userId?.let {
            showFollowers.event = userId
        }
    }

    fun showFollowings(userId: User.Id?) {
        userId?.let {
            showFollowings.event = userId
        }
    }

    fun showProfile(account: Account?) {
        if (account == null) {
            logger.debug("showProfile account未取得のためキャンセル")
            return
        }
        showProfile.event = account
    }


    fun signOut(account: Account) {
        viewModelScope.launch {
            signOutUseCase(account).onFailure { e ->
                logger.error("ログアウト処理失敗", e)
            }
        }
    }

    fun addPage(page: Page) {
        viewModelScope.launch {
            try {
                accountStore.addPage(page)
            } catch (e: Throwable) {
                logger.error("pageの追加に失敗", e = e)
            }
        }
    }

    fun removePage(page: Page) {
        viewModelScope.launch {
            try {
                accountStore.removePage(page)
            } catch (e: Throwable) {
                logger.error("pageの削除に失敗", e = e)
            }
        }
    }

}

data class AccountInfo(
    val account: Account,
    val user: User?,
    val instanceMeta: Meta?,
    val isCurrentAccount: Boolean
)

data class AccountViewModelUiState(
    val currentAccount: Account? = null,
    val accounts: List<AccountInfo> = emptyList(),
) {
    val currentAccountInfo: AccountInfo? by lazy {
        accounts.firstOrNull {
            it.account.accountId == currentAccount?.accountId
        }
    }
}