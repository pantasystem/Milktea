package net.pantasystem.milktea.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common_android.resource.StringSource
import net.pantasystem.milktea.model.account.Account
import javax.inject.Inject

@HiltViewModel
class SearchTopViewModel @Inject constructor(
    accountStore: AccountStore,
    private val searchTopTabsFactory: SearchTopTabsFactory,
) : ViewModel() {
    private val currentAccount = accountStore.observeCurrentAccount.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null
    )

    val uiState = currentAccount.map {
        SearchTopUiState(
            currentAccount = it,
            tabItems = searchTopTabsFactory.create(it)
        )
    }

}

data class SearchTopUiState(
    val currentAccount: Account?,
    val tabItems: List<SearchTopTabItem>,
)

data class SearchTopTabItem(
    val title: StringSource,
    val type: TabType,
) {
    enum class TabType {
        MisskeyFeatured,
        MastodonTrends,
        MastodonUserSuggestions,
        MisskeyExploreUsers,
        MisskeyExploreFediverseUsers,
        UserSuggestionByReaction,
        HashtagTrend,
    }
}