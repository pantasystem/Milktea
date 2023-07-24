package net.pantasystem.milktea.search.trend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.asLoadingStateFlow
import net.pantasystem.milktea.common.initialState
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.hashtag.HashTag
import net.pantasystem.milktea.model.hashtag.HashtagRepository
import javax.inject.Inject

@HiltViewModel
class TrendViewModel @Inject constructor(
    val accountStore: AccountStore,
    val hashtagRepository: HashtagRepository,
) : ViewModel() {
    private val currentAccount = accountStore.observeCurrentAccount.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null,
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val trends = currentAccount.filterNotNull().flatMapLatest {
        suspend {
            hashtagRepository.trends(it.accountId).getOrThrow()
        }.asLoadingStateFlow()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ResultState.initialState(),
    )

    val uiState = combine(currentAccount, trends) { ca, t ->
        TrendUiState(
            currentAccount = ca,
            trendTags = t
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        TrendUiState(
            null,
            ResultState.initialState(),
        )
    )

}

data class TrendUiState(
    val currentAccount: Account?,
    val trendTags: ResultState<List<HashTag>>,
)