package jp.panta.misskeyandroidclient.model.account

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class AccountStore @Inject constructor(
    val accountRepository: AccountRepository,
){
    private val _state = MutableStateFlow<AccountState>(AccountState.Loading)
    val state: StateFlow<AccountState> = _state


    suspend fun addAccount(account: Account) {
        accountRepository.add(account, true)
        val state = state.value
        if (state is AccountState.Unauthorized) {
            _state.value = state.changeCurrent(account)
        }
    }

    suspend fun setCurrent(account: Account) {
        accountRepository.setCurrentAccount(account)
        _state.value = AccountState.Authorized(account)
    }

    suspend fun initialize() {
        runCatching {

        }
    }


}