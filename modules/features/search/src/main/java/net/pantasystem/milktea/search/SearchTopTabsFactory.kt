package net.pantasystem.milktea.search

import net.pantasystem.milktea.common_android.resource.StringSource
import net.pantasystem.milktea.model.account.Account
import javax.inject.Inject

class SearchTopTabsFactory @Inject constructor() {

    fun create(account: Account?): List<SearchTopTabItem> {
        return when(account?.instanceType) {
            Account.InstanceType.MISSKEY, Account.InstanceType.FIREFISH -> {
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
    }
}