package net.pantasystem.milktea.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.instance.InstanceInfoService
import net.pantasystem.milktea.model.instance.InstanceInfoType
import net.pantasystem.milktea.model.instance.online.user.count.OnlineUserCountRepository
import net.pantasystem.milktea.model.instance.online.user.count.OnlineUserCountResult
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject

@HiltViewModel
class AccountScreenViewModel @Inject constructor(
    accountStore: AccountStore,
    private val userRepository: UserRepository,
    private val userDataSource: UserDataSource,
    private val onlineUserCountRepository: OnlineUserCountRepository,
    private val instanceInfoService: InstanceInfoService,
    private val loggerFactory: Logger.Factory,
) : ViewModel() {

    private val logger by lazy {
        loggerFactory.create("AccountVM")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val user = accountStore.observeCurrentAccount.filterNotNull().flatMapLatest {
        userDataSource.observe(User.Id(it.accountId, it.remoteId))
    }.catch {
        logger.error("observe current accounts user error", it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val currentAccount = accountStore.observeCurrentAccount.catch {
        logger.error("observe current account error", it)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null
    )

    private val instanceInfo = accountStore.observeCurrentAccount.filterNotNull().map {
        instanceInfoService.find(it.normalizedInstanceUri).getOrThrow()
    }.catch {
        logger.error("インスタンス情報の取得に失敗", it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val onlineUserCount = accountStore.observeCurrentAccount.filterNotNull().map {
        onlineUserCountRepository.find(it.accountId).onFailure { e ->
            logger.error("オンラインユーザー数の取得に失敗", e)
        }.getOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val uiState = combine(currentAccount, user, instanceInfo, onlineUserCount) { a, u, info, count ->
        AccountUiState(
            currentAccount = a,
            userInfo = u,
            instanceInfo = info,
            onlineUserCount = count,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccountUiState())

    init {
        accountStore.observeCurrentAccount.filterNotNull().onEach { account ->
            userRepository.sync(User.Id(account.accountId, account.remoteId)).onFailure {
                logger.error("sync accounts user info error", it)
            }
        }.launchIn(viewModelScope)
    }
}

data class AccountUiState(
    val currentAccount: Account? = null,
    val userInfo: User? = null,
    val instanceInfo: InstanceInfoType? = null,
    val onlineUserCount: OnlineUserCountResult? = null,
)