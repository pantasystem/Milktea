package net.pantasystem.milktea.setting.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.asLoadingStateFlow
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.renote.mute.RenoteMute
import net.pantasystem.milktea.model.user.renote.mute.RenoteMuteRepository
import javax.inject.Inject

@HiltViewModel
class RenoteMuteSettingViewModel @Inject constructor(
    accountStore: AccountStore,
    private val renoteMuteRepository: RenoteMuteRepository,
    private val userDataSource: UserDataSource,
) : ViewModel() {

    private val currentAccount = accountStore.observeCurrentAccount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val renoteMutes = currentAccount.filterNotNull().flatMapLatest {
        renoteMuteRepository.observeBy(it.accountId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val relatedUsers =
        combine(currentAccount.filterNotNull(), renoteMutes) { account, mutes ->
            AccountWithMutes(
                account,
                mutes,
            )
        }.flatMapLatest {
            userDataSource.observeIn(it.account.accountId, it.userIds)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val refreshEvent = MutableSharedFlow<Account>(extraBufferCapacity = 5)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val syncState = refreshEvent.flatMapLatest {
        suspend {
            renoteMuteRepository.syncBy(it.accountId)
        }.asLoadingStateFlow()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ResultState.Loading(StateContent.NotExist())
    )

    val uiState = combine(
        currentAccount,
        renoteMutes,
        relatedUsers,
        syncState,
    ) { account, renoteMutes, users, syncState ->
        RenoteMuteSettingUiState(
            currentAccount = account,
            renoteMutes = renoteMutes.map { renoteMute ->
                RenoteMuteWithUser(
                    renoteMute = renoteMute,
                    user = users.firstOrNull {
                        it.id == renoteMute.userId
                    }
                )
            }

        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        RenoteMuteSettingUiState()
    )

    private val _errors = MutableSharedFlow<Throwable>(
        extraBufferCapacity = 100,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val errors = _errors.asSharedFlow()

    fun onRemoveRenoteMute(renoteMute: RenoteMute) {
        viewModelScope.launch {
            renoteMuteRepository.delete(renoteMute.userId).onFailure {
                _errors.tryEmit(it)
            }
        }
    }

    fun onRefresh(account: Account) {
        refreshEvent.tryEmit(account)
    }
}


data class RenoteMuteSettingUiState(
    val currentAccount: Account? = null,
    val renoteMutes: List<RenoteMuteWithUser> = emptyList(),
    val syncState: ResultState<Unit> = ResultState.Fixed(StateContent.NotExist()),
)

data class RenoteMuteWithUser(
    val renoteMute: RenoteMute,
    val user: User?,
)

private data class AccountWithMutes(
    val account: Account,
    val mutes: List<RenoteMute>,
) {
    val userIds = mutes.map {
        it.userId.id
    }
}