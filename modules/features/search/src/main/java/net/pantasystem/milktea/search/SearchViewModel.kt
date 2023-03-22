package net.pantasystem.milktea.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common.*
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.CurrentAccountWatcher
import net.pantasystem.milktea.model.hashtag.HashtagRepository
import net.pantasystem.milktea.model.search.SearchHistory
import net.pantasystem.milktea.model.search.SearchHistoryRepository
import net.pantasystem.milktea.model.user.Acct
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    loggerFactory: Logger.Factory,
    accountRepository: AccountRepository,
    private val userRepository: UserRepository,
    private val userDataSource: UserDataSource,
    private val hashtagRepository: HashtagRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val logger by lazy {
        loggerFactory.create("SearchViewModel")
    }

    val keyword = savedStateHandle.getStateFlow<String>("keyword", "")
    private val currentAccountWatcher = CurrentAccountWatcher(null, accountRepository)

    @OptIn(ExperimentalCoroutinesApi::class)
    val hashtagResult = keyword.filter {
        it.isHashTagFormat()
    }.map {
        it.substring(1, it.length)
    }.flatMapLatest {
        suspend {
            hashtagRepository.search(
                currentAccountWatcher.getAccount().normalizedInstanceUri,
                it
            ).getOrThrow()
        }.asLoadingStateFlow()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ResultState.Loading(StateContent.NotExist())
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchUserResult = keyword.filterNot {
        it.isHashTagFormat()
    }.flatMapLatest {
        suspend {
            userRepository.syncByUserName(
                currentAccountWatcher.getAccount().accountId,
                Acct(it).userName,
                Acct(it).host
            )
            userRepository.searchByNameOrUserName(currentAccountWatcher.getAccount().accountId, it)
        }.asLoadingStateFlow()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ResultState.Loading(StateContent.NotExist())
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val users = searchUserResult.map {
        (it.content as? StateContent.Exist)?.rawContent ?: emptyList()
    }.flatMapLatest { users ->
        val ids = users.map { it.id.id }
        userDataSource.observeIn(currentAccountWatcher.getAccount().accountId, ids)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private val searchHistories = suspend {
        currentAccountWatcher.getAccount()
    }.asFlow().flatMapLatest {
        searchHistoryRepository.observeBy(it.accountId, limit = 10)
    }.catch {
        logger.error("fetch search histories failed", it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val states = combine(hashtagResult, searchUserResult) { h, s ->
        States(
            hashtagsState = h,
            searchUserState = s,
        )
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), States(
            searchUserState = searchUserResult.value,
            hashtagsState = hashtagResult.value,
        )
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val account = currentAccountWatcher.account.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null
    )

    val uiState = combine(
        keyword,
        states,
        searchHistories,
        account,
        users
    ) { keyword, states, histories, a, users ->
        SearchUiState(
            keyword,
            (states.hashtagsState.content as? StateContent.Exist)?.rawContent ?: emptyList(),
            users,
            histories,
            searchUserState = states.searchUserState,
            hashtagsState = states.hashtagsState,
            accountHost = a?.getHost()
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        SearchUiState(
            keyword.value,
            emptyList(),
            emptyList(),
            emptyList(),
            searchUserResult.value,
            hashtagResult.value,
            null
        )
    )

    fun onInputKeyword(word: String) {
        savedStateHandle["keyword"] = word
    }

    suspend fun onQueryTextSubmit(word: String) {
        if (word.isBlank()) {
            return
        }
        runCancellableCatching {
            searchHistoryRepository.add(
                SearchHistory(
                    accountId = currentAccountWatcher.getAccount().accountId,
                    keyword = word,
                )
            ).getOrThrow()
        }.onFailure {
            logger.error("検索履歴の保存に失敗", it)
        }
    }

    fun deleteSearchHistory(id: Long) = viewModelScope.launch {
        searchHistoryRepository.delete(id).onFailure {
            logger.error("検索履歴の削除に失敗", it)
        }
    }
}

data class SearchUiState(
    val keyword: String,
    val hashtags: List<String>,
    val users: List<User>,
    val history: List<SearchHistory>,
    val searchUserState: ResultState<List<User>>,
    val hashtagsState: ResultState<List<String>>,
    val accountHost: String?,
)

private data class States(
    val searchUserState: ResultState<List<User>>,
    val hashtagsState: ResultState<List<String>>,
)

private fun String.isHashTagFormat(): Boolean {
    return startsWith("#") && length > 1
}