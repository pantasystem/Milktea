package net.pantasystem.milktea.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common_android.resource.StringSource
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import javax.inject.Inject

@HiltViewModel
class SearchResultViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val accountStore: AccountStore,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    companion object {
        const val EXTRA_KEYWORD =
            "net.pantasystem.milktea.search.SearchResultViewModel.EXTRA_KEYWORD"
    }

    private val keyword = savedStateHandle.getStateFlow(EXTRA_KEYWORD, "")

    val uiState = combine(
        accountStore.observeCurrentAccount,
        keyword
    ) { currentAccount, keyword ->
        SearchResultUiState(
            currentAccount = currentAccount,
            keyword = keyword
        )
    }

    fun setKeyword(q: String) {
        savedStateHandle[EXTRA_KEYWORD] = q
    }
}

data class SearchResultUiState(
    val currentAccount: Account?,
    val keyword: String,
) {
    val isTag: Boolean = keyword.startsWith("#")

    val tabItems: List<SearchResultTabItem> = when (currentAccount?.instanceType) {
        Account.InstanceType.MISSKEY -> listOfNotNull(
            if (isTag) {
                SearchResultTabItem(
                    title = StringSource(R.string.timeline),
                    type = SearchResultTabItem.Type.SearchMisskeyPostsByTag,
                    query = keyword.replace("#", "")
                )
            } else {
                SearchResultTabItem(
                    title = StringSource(R.string.timeline),
                    type = SearchResultTabItem.Type.SearchMisskeyPosts,
                    query = keyword,
                )
            },
            if (isTag) SearchResultTabItem(
                title = StringSource(R.string.media),
                type = SearchResultTabItem.Type.SearchMisskeyPostsWithFilesByTag,
                query = keyword.replace("#", "")
            ) else null,
            SearchResultTabItem(
                title = StringSource(R.string.user),
                type = SearchResultTabItem.Type.SearchMisskeyUsers,
                query = keyword,
            )
        )
        Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> listOfNotNull(
            if (isTag) {
                SearchResultTabItem(
                    title = StringSource(R.string.timeline),
                    type = SearchResultTabItem.Type.SearchMastodonPostsByTag,
                    query = keyword.replace("#", ""),
                )
            } else {
                SearchResultTabItem(
                    title = StringSource(R.string.timeline),
                    type = SearchResultTabItem.Type.SearchMastodonPosts,
                    query = keyword,
                )
            },
            SearchResultTabItem(
                title = StringSource(R.string.user),
                type = SearchResultTabItem.Type.SearchMastodonUsers,
                query = keyword,
            )

        )
        null -> emptyList()
    }
}

data class SearchResultTabItem(
    val title: StringSource,
    val type: Type,
    val query: String,
) {
    enum class Type {
        SearchMisskeyPosts,
        SearchMisskeyPostsByTag,
        SearchMisskeyPostsWithFilesByTag,
        SearchMisskeyUsers,
        SearchMastodonPosts,
        SearchMastodonPostsByTag,
        SearchMastodonUsers,
    }
}