package net.pantasystem.milktea.model.filter

import kotlinx.datetime.Clock
import net.pantasystem.milktea.model.account.page.Pageable
import javax.inject.Inject

class GetMatchContextFilters @Inject constructor() {
    operator fun invoke(pageable: Pageable, filters: List<MastodonWordFilter>): List<MastodonWordFilter> {
        val now = Clock.System.now()
        return filters.filter {
            it.expiresAt == null || it.expiresAt > now
        }.filter { filter ->
            filter.isContextHome && pageable is Pageable.Mastodon.HomeTimeline
                    || filter.isContextNotifications && pageable is Pageable.Notification
                    || filter.isContextPublic && pageable is Pageable.Mastodon.PublicTimeline
                    || filter.isContextPublic && pageable is Pageable.Mastodon.LocalTimeline
                    || filter.isContextAccount && pageable is Pageable.Mastodon.UserTimeline
                    || filter.isContextThread && pageable is Pageable.Show
                    || filter.isContextHome && pageable is Pageable.Mastodon.HomeTimeline
        }
    }
}
