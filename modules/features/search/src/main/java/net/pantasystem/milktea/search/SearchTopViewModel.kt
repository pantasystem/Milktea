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
    val accountStore: AccountStore,
) : ViewModel() {
    private val currentAccount = accountStore.observeCurrentAccount.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null
    )

    val uiState = currentAccount.map {
        SearchTopUiState(
            currentAccount = it,
            tabItems = when(it?.instanceType) {
                Account.InstanceType.MISSKEY -> {
                    listOf(
                        SearchTopTabItem(
                            StringSource(R.string.title_featured),
                            SearchTopTabItem.TabType.MisskeyFeatured,
                        ),
                        SearchTopTabItem(
                            StringSource(R.string.explore),
                            SearchTopTabItem.TabType.MisskeyExploreUsers,
                        ),
                        SearchTopTabItem(
                            StringSource(R.string.explore_fediverse),
                            SearchTopTabItem.TabType.MisskeyExploreFediverseUsers,
                        ),
                        SearchTopTabItem(
                            StringSource(R.string.suggestion_users),
                            SearchTopTabItem.TabType.UserSuggestionByReaction,
                        ),
                        SearchTopTabItem(
                            StringSource(R.string.trending_tag),
                            SearchTopTabItem.TabType.HashtagTrend,
                        )
                    )
                }
                Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                    listOf(
                        SearchTopTabItem(
                            StringSource(R.string.title_featured),
                            SearchTopTabItem.TabType.MastodonTrends,
                        ),
                        SearchTopTabItem(
                            StringSource(R.string.suggestion_users),
                            SearchTopTabItem.TabType.MastodonUserSuggestions,
                        ),
                        SearchTopTabItem(
                            StringSource(R.string.trending_tag),
                            SearchTopTabItem.TabType.HashtagTrend,
                        )
                    )
                }
                null -> emptyList()
            }
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