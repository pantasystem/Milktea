package net.pantasystem.milktea.user.followrequests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.setting.Config
import net.pantasystem.milktea.model.setting.DefaultConfig
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.model.user.FollowRequestRepository
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.follow.requests.FollowRequestPagingStore
import javax.inject.Inject

@HiltViewModel
class FollowRequestsViewModel @Inject constructor(
    accountRepository: AccountRepository,
    followRequestPagingStoreFactory: FollowRequestPagingStore.Factory,
    configRepository: LocalConfigRepository,
    private val accountStore: AccountStore,
    private val userDataSource: UserDataSource,
    private val followRequestRepository: FollowRequestRepository,
) : ViewModel() {

    private val _errors = MutableSharedFlow<Throwable>(extraBufferCapacity = 10, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val errors = _errors.asSharedFlow()

    private val pagingStore = followRequestPagingStoreFactory.create {
        accountRepository.getCurrentAccount().getOrThrow()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val users = pagingStore.state.map { state ->
        (state.content as? StateContent.Exist)?.rawContent ?: emptyList()
    }.flatMapLatest { ids ->
        accountStore.observeCurrentAccount.filterNotNull().flatMapLatest { account ->
            userDataSource.observeIn(account.accountId, ids.map { it.id })
        }.onStart {
            emit(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val state = combine(users, pagingStore.state) { users, state ->
        state.convert {
            users
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PageableState.Loading.Init())

    val uiState = combine(
        accountStore.observeCurrentAccount.filterNotNull(),
        state,
        users,
        configRepository.observe()
    ) { ac, state, users, config ->
        FollowRequestsUiState(
            ac,
            users,
            state,
            config
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        FollowRequestsUiState(
            null,
            emptyList(),
            PageableState.Loading.Init(),
            DefaultConfig.config,
        )
    )

    init {
        accountStore.observeCurrentAccount.distinctUntilChanged().onEach {
            onRefresh()
        }.launchIn(viewModelScope)
    }

    fun onRefresh() {
        viewModelScope.launch {
            pagingStore.clear()
            pagingStore.loadPrevious().onFailure {
                _errors.tryEmit(it)
            }
        }
    }

    fun onAccept(userId: User.Id) {
        viewModelScope.launch {
            runCancellableCatching {
                followRequestRepository.accept(userId)
            }.onFailure {
                _errors.tryEmit(it)
            }.onSuccess {
                onRefresh()
            }
        }
    }

    fun onReject(userId: User.Id) {
        viewModelScope.launch {
            runCancellableCatching {
                followRequestRepository.reject(userId)
            }.onFailure {
                _errors.tryEmit(it)
            }.onSuccess {
                onRefresh()
            }
        }
    }

}

data class FollowRequestsUiState(
    val currentAccount: Account?,
    val users: List<User>,
    val pagingState: PageableState<List<User>>,
    val config: Config,
)