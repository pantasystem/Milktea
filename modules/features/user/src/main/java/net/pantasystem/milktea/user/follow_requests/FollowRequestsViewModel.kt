package net.pantasystem.milktea.user.follow_requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.CurrentAccountWatcher
import net.pantasystem.milktea.model.user.FollowRequestRepository
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.follow_requests.FollowRequestPagingStore
import javax.inject.Inject

@HiltViewModel
class FollowRequestsViewModel @Inject constructor(
    accountRepository: AccountRepository,
    followRequestPagingStoreFactory: FollowRequestPagingStore.Factory,
    private val userDataSource: UserDataSource,
    private val followRequestRepository: FollowRequestRepository,
) : ViewModel() {

    private val _errors = MutableSharedFlow<Throwable>(extraBufferCapacity = 10, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val accountWatcher = CurrentAccountWatcher(null, accountRepository)
    private val pagingStore = followRequestPagingStoreFactory.create {
        accountWatcher.getAccount()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val users = pagingStore.state.map { state ->
        (state.content as? StateContent.Exist)?.rawContent ?: emptyList()
    }.flatMapLatest { ids ->
        accountWatcher.account.flatMapLatest { account ->
            userDataSource.observeIn(account.accountId, ids.map { it.id })
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val state = combine(users, pagingStore.state) { users, state ->
        state.convert {
            users
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PageableState.Loading.Init())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState = combine(accountWatcher.account, state, users) { ac, state, users ->
        FollowRequestsUiState(
            ac,
            users,
            state
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        FollowRequestsUiState(null, emptyList(), PageableState.Loading.Init())
    )

    fun refresh() {
        viewModelScope.launch {
            pagingStore.clear()
            pagingStore.loadPrevious().onFailure {
                _errors.tryEmit(it)
            }
        }
    }

    fun accept(userId: User.Id) {
        viewModelScope.launch {
            runCancellableCatching {
                followRequestRepository.accept(userId)
            }.onFailure {
                _errors.tryEmit(it)
            }.onSuccess {
                refresh()
            }
        }
    }

    fun reject(userId: User.Id) {
        viewModelScope.launch {
            runCancellableCatching {
                followRequestRepository.reject(userId)
            }.onFailure {
                _errors.tryEmit(it)
            }.onSuccess {
                refresh()
            }
        }
    }

}

data class FollowRequestsUiState(
    val currentAccount: Account?,
    val users: List<User>,
    val pagingState: PageableState<List<User>>,
)