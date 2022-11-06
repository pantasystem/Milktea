package net.pantasystem.milktea.user.search

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
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserRepository
import java.util.regex.Pattern
import javax.inject.Inject


data class SearchUser(
    val word: String,
    val host: String?
) {
    val isUserName: Boolean
        get() = Pattern.compile("""^[a-zA-Z_\-0-9]+$""")
            .matcher(word)
            .find()

}

@HiltViewModel
class SearchUserViewModel @Inject constructor(
    accountStore: AccountStore,
    loggerFactory: Logger.Factory,
    private val userRepository: UserRepository,
    userDataSource: UserDataSource,
) : ViewModel() {

    private val logger = loggerFactory.create("SearchUserViewModel")


    private val searchUserRequests = MutableStateFlow<SearchUser>(SearchUser("", null))


    @OptIn(ExperimentalCoroutinesApi::class)
    val syncByUserNameLoadingState = accountStore.observeCurrentAccount.filterNotNull()
        .flatMapLatest { account ->
            searchUserRequests.flatMapLatest {
                suspend {
                    userRepository
                        .syncByUserName(
                            accountId = account.accountId,
                            userName = it.word,
                            host = it.host
                        )
                }.asLoadingStateFlow()
            }
        }.flowOn(Dispatchers.IO).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            ResultState.Loading(StateContent.NotExist())
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredByNameLoadingState = accountStore.observeCurrentAccount.filterNotNull()
        .flatMapLatest { account ->
            searchUserRequests.flatMapLatest {
                suspend {
                    userRepository.searchByNameOrAcct(account.accountId, it.word)
                }.asLoadingStateFlow()
            }
        }.map { state ->
            state.convert { list ->
                list.map { it.id }
            }
        }.flowOn(Dispatchers.IO).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            ResultState.Loading(StateContent.NotExist())
        )

    val searchState =
        combine(
            filteredByNameLoadingState,
            syncByUserNameLoadingState
        ) { users1, remoteSyncState ->
            val content1 = (users1.content as? StateContent.Exist?)?.rawContent
                ?: emptyList()

            val isNotExists =
                users1.content is StateContent.NotExist
            val isLoading = users1 is ResultState.Loading && remoteSyncState is ResultState.Loading
            val isFailure = users1 is ResultState.Error && remoteSyncState is ResultState.Error
            val content = if (isNotExists) StateContent.NotExist() else StateContent.Exist(content1)
            val throwable = (users1 as? ResultState.Error?)?.throwable
                ?: (remoteSyncState as? ResultState.Error?)?.throwable
            if (isLoading) {
                ResultState.Loading(content)
            } else if (isFailure) {
                ResultState.Error(content, throwable = throwable!!)
            } else {
                ResultState.Fixed(content)
            }
        }.catch { error ->
            logger.info("ユーザー検索処理に失敗しました", e = error)
        }.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5_000), ResultState.Fixed(
                StateContent.NotExist()
            )
        )


    val uiState = combine(searchState, searchUserRequests) { state, request ->
        SearchUserUiState(request, state)
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        SearchUserUiState(
            SearchUser("", null),
            ResultState.Loading(StateContent.NotExist())
        )
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val users = searchState.map {
        (it.content as? StateContent.Exist)?.rawContent
            ?: emptyList()
    }.flatMapLatest { ids ->
        userDataSource.observeIn(accountStore.currentAccountId!!, ids.map { it.id }).map { users ->
            users.mapNotNull { user ->
                user as? User.Detail?
            }
        }
    }.catch {
        logger.error("observe error", it)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    val errors = syncByUserNameLoadingState.map {
        it as? ResultState.Error?
    }.mapNotNull {
        it?.throwable
    }.distinctUntilChanged().shareIn(viewModelScope, SharingStarted.Lazily)

    fun setUserName(text: String) {
        searchUserRequests.update {
            it.copy(word = text)
        }
    }

    fun setHost(text: String?) {
        searchUserRequests.update {
            it.copy(host = text)
        }
    }

    fun search() {
        searchUserRequests.update {
            it
        }
    }


}

data class SearchUserUiState(
    val query: SearchUser,
    val result: ResultState<List<User.Id>>,
)