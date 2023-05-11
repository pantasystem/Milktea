package net.pantasystem.milktea.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.mapCancellableCatching
import net.pantasystem.milktea.common_android.resource.StringSource
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.page.PageableTemplate
import net.pantasystem.milktea.model.user.Acct
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject

@HiltViewModel
class SearchResultViewModel @Inject constructor(
    loggerFactory: Logger.Factory,
    private val accountStore: AccountStore,
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    companion object {
        const val EXTRA_KEYWORD =
            "net.pantasystem.milktea.search.SearchResultViewModel.EXTRA_KEYWORD"
        const val EXTRA_ACCT = "net.pantasystem.milktea.search.SearchResultActivity.EXTRA_ACCT"

    }

    private val logger by lazy {
        loggerFactory.create("SearchResultVM")
    }

    private val keyword = savedStateHandle.getStateFlow(EXTRA_KEYWORD, "")
    private val acct = savedStateHandle.getStateFlow<String?>(EXTRA_ACCT, null)
    private val user = combine(
        acct,
        accountStore.observeCurrentAccount.filterNotNull()
    ) { acct, ac ->
        userRepository.findByUserName(
            ac.accountId,
            Acct(acct ?: "").userName,
            Acct(acct ?: "").host
        )
    }.catch {
        logger.debug("ユーザの情報の取得に失敗", e = it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val uiState = combine(
        accountStore.observeCurrentAccount,
        keyword,
        acct,
        user
    ) { currentAccount, keyword, acct, user ->
        SearchResultUiState(
            currentAccount = currentAccount,
            keyword = keyword,
            acct = acct,
            user = user
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        SearchResultUiState(null, "", null, null)
    )

    fun setKeyword(q: String) {
        savedStateHandle[EXTRA_KEYWORD] = q
    }

    fun setAcct(acct: String?) {
        savedStateHandle[EXTRA_ACCT] = acct
    }
    fun toggleAddToTab() {
        viewModelScope.launch {
            val keyword = savedStateHandle.get<String>(EXTRA_KEYWORD) ?: return@launch
            accountRepository.getCurrentAccount().mapCancellableCatching { account ->
                val exists = account.pages.firstOrNull {
                    (it.pageParams.tag == keyword
                            || it.pageParams.query == keyword)
                }
                if (exists == null) {
                    val page = if (keyword.startsWith("#")) {
                        PageableTemplate(account).tag(keyword)
                    } else {
                        PageableTemplate(account).search(keyword)
                    }
                    accountStore.addPage(page)
                } else {
                    accountStore.removePage(exists)
                }
            }
        }
    }
}

data class SearchResultUiState(
    val currentAccount: Account?,
    val keyword: String,
    val acct: String?,
    val user: User?,
) {
    val isTag: Boolean = keyword.startsWith("#") && acct == null

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
                    userId = user?.id?.id,
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
                userId = user?.id?.id,
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
                    userId = user?.id?.id,
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
    val userId: String? = null,
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