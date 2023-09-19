package net.pantasystem.milktea.common_android_ui.account.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.instance.InstanceInfoService
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserRepository

class AccountViewModelUiStateHelper(
    currentAccountFlow: Flow<Account?>,
    accountStore: AccountStore,
    private val userRepository: UserRepository,
    private val instanceInfoService: InstanceInfoService,
    viewModelScope: CoroutineScope,
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val users = accountStore.observeAccounts.flatMapLatest { accounts ->
        val flows = accounts.map {
            userRepository.observe(User.Id(it.accountId, it.remoteId)).flowOn(Dispatchers.IO)
        }
        combine(flows) {
            it.toList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val metaList = accountStore.observeAccounts.flatMapLatest { accounts ->
        instanceInfoService.observeIn(accounts.map { it.normalizedInstanceUri })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val accountWithUserList = combine(
        accountStore.observeAccounts,
        users,
        currentAccountFlow,
        metaList,
    ) { accounts, users, current, metaList ->
        accounts.toAccountInfoList(current, metaList, users)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState = combine(
        currentAccountFlow,
        accountWithUserList
    ) { current, accounts ->
        AccountViewModelUiState(
            currentAccount = current,
            accounts = accounts
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccountViewModelUiState())

}