package net.pantasystem.milktea.user.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.asLoadingStateFlow
import net.pantasystem.milktea.common.initialState
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserRepository
import java.util.regex.Pattern
import javax.inject.Inject


@HiltViewModel
class SearchUserViewModel @Inject constructor(
    accountStore: AccountStore,
    loggerFactory: Logger.Factory,
    private val userRepository: UserRepository,
    userDataSource: UserDataSource,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    companion object {
        const val EXTRA_ACCOUNT_ID = "SearchUserViewModel.EXTRA_ACCOUNT_ID"
    }

    private val logger = loggerFactory.create("SearchUserViewModel")


    private val searchUserRequests = MutableStateFlow<SearchUser>(SearchUser("", null))

    @OptIn(ExperimentalCoroutinesApi::class)
    private val account = savedStateHandle.getStateFlow<Long?>(EXTRA_ACCOUNT_ID, null).flatMapLatest { accountId ->
        accountStore.getOrCurrent(accountId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)



    @OptIn(ExperimentalCoroutinesApi::class)
    private val syncByUserNameLoadingState = account.filterNotNull()
        .flatMapLatest { account ->
            searchUserRequests.flatMapLatest { query ->
                suspend {
                    userRepository.syncByUserName(
                        account.accountId,
                        query.getNameOrUserName(),
                        host = query.getHost()
                    ).getOrThrow()
                }.asLoadingStateFlow().map {
                    SyncRemoteResult.from(account, query, it)
                }
            }
        }


    @OptIn(ExperimentalCoroutinesApi::class)
    private val filteredByNameLoadingState =
        syncByUserNameLoadingState.distinctUntilChanged().flatMapLatest {
            suspend {
                userRepository.searchByNameOrUserName(it.account.accountId, it.word, host = it.host)
            }.asLoadingStateFlow()
        }.map { state ->
            state.convert { list ->
                list.map { it.id }
            }
        }.flowOn(Dispatchers.IO).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            ResultState.initialState(),
        )

    val searchState = filteredByNameLoadingState.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), ResultState.Fixed(
            StateContent.NotExist()
        )
    )


    val uiState = combine(searchState, searchUserRequests, account) { state, request, a ->
        SearchUserUiState(request, state, a)
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        SearchUserUiState(
            SearchUser("", null),
            ResultState.initialState(),
            account = null
        )
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val users = searchState.map {
        (it.content as? StateContent.Exist)?.rawContent
            ?: emptyList()
    }.flatMapLatest { ids ->
        userDataSource.observeIn(account.value?.accountId!!, ids.map { it.id }).map { users ->
            users.mapNotNull { user ->
                user as? User.Detail?
            }
        }
    }.catch {
        logger.error("observe error", it)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val currentAccount = account.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null
    )

    fun setUserName(text: String) {
        searchUserRequests.update {
            it.copy(word = text)
        }
    }

    fun setHost(text: String?) {
        searchUserRequests.update {
            it.copy(sourceHost = text)
        }
    }

    fun search() {
        searchUserRequests.update {
            it
        }
    }


}


data class SearchUser(
    val word: String,
    val sourceHost: String?
) {
    val isUserName: Boolean
        get() = Pattern.compile("""^[a-zA-Z_\-0-9]+$""")
            .matcher(word)
            .find()

    fun getNameOrUserName(): String {
        return word.split("@").filterNot {
            it.isBlank()
        }.firstOrNull() ?: word
    }

    fun getHost(): String? {
        return word.split("@").filterNot {
            it.isBlank()
        }.getOrElse(1) {
            sourceHost
        }
    }
}

data class SearchUserUiState(
    val query: SearchUser,
    val result: ResultState<List<User.Id>>,
    val account: Account?,
)

data class SyncRemoteResult(
    val word: String = "",
    val host: String? = null,
    val isSuccess: Boolean = false,
    val isInitial: Boolean = true,
    val account: Account,
) {
    companion object {
        fun from(account: Account, query: SearchUser, state: ResultState<Unit>): SyncRemoteResult {
            return when (state) {
                is ResultState.Error -> SyncRemoteResult(
                    query.getNameOrUserName(),
                    query.getHost(),
                    isSuccess = false,
                    isInitial = false,
                    account = account
                )
                is ResultState.Fixed -> SyncRemoteResult(
                    query.getNameOrUserName(),
                    query.getHost(),
                    isSuccess = true,
                    isInitial = false,
                    account = account
                )
                is ResultState.Loading -> SyncRemoteResult(
                    query.getNameOrUserName(),
                    query.getHost(),
                    isSuccess = false,
                    isInitial = false,
                    account = account
                )
            }
        }
    }
}