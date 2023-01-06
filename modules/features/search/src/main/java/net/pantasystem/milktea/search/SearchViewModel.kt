package net.pantasystem.milktea.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.asLoadingStateFlow
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.CurrentAccountWatcher
import net.pantasystem.milktea.model.hashtag.HashtagRepository
import net.pantasystem.milktea.model.user.Acct
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userDataSource: UserDataSource,
    private val hashtagRepository: HashtagRepository,
    val accountRepository: AccountRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

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
                currentAccountWatcher.getAccount().normalizedInstanceDomain,
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

    val uiState = combine(
        keyword,
        hashtagResult,
        searchUserResult,
        users
    ) { keyword, hashtags, searchUserResult, users ->
        SearchUiState(
            keyword,
            (hashtags.content as? StateContent.Exist)?.rawContent ?: emptyList(),
            users,
            searchUserResult,
            hashtags,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        SearchUiState(
            keyword.value,
            emptyList(),
            emptyList(),
            searchUserResult.value,
            hashtagResult.value
        )
    )

    fun onInputKeyword(word: String) {
        savedStateHandle["keyword"] = word
    }

}

data class SearchUiState(
    val keyword: String,
    val hashtags: List<String>,
    val users: List<User>,
    val searchUserState: ResultState<List<User>>,
    val hashtagsState: ResultState<List<String>>
)

private fun String.isHashTagFormat(): Boolean {
    return startsWith("#") && length > 1
}