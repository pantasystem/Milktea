package net.pantasystem.milktea.model.filter

import kotlinx.datetime.Clock
import net.pantasystem.milktea.model.account.page.Pageable
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

internal class GetMatchContextFiltersTest {

    @Test
    fun giveExpiredFilters() {
        val expiredTimes = Clock.System.now() - 3.minutes

        val expected = listOf(
            MastodonWordFilter(
                id = MastodonWordFilter.Id(
                    accountId = 0,
                    filterId = ""
                ),
                phrase = "",
                context = listOf(MastodonWordFilter.FilterContext.Home),
                wholeWord = false,
                expiresAt = expiredTimes + 1.days,
                irreversible = false
            ),
            MastodonWordFilter(
                id = MastodonWordFilter.Id(
                    accountId = 0,
                    filterId = ""
                ),
                phrase = "",
                context = listOf(MastodonWordFilter.FilterContext.Home),
                wholeWord = false,
                expiresAt = null,
                irreversible = false
            )
        )
        val filters = listOf(
            MastodonWordFilter(
                id = MastodonWordFilter.Id(
                    accountId = 0,
                    filterId = ""
                ),
                phrase = "",
                context = listOf(MastodonWordFilter.FilterContext.Home),
                wholeWord = false,
                expiresAt = expiredTimes,
                irreversible = false
            ),
        ) + expected
        val result = GetMatchContextFilters().invoke(
            Pageable.Mastodon.HomeTimeline,
            filters
        )
        assertEquals(expected, result)
    }

    @Test
    fun giveHasHomeContextAndHomePageable() {

        val expected = listOf(
            MastodonWordFilter(
                id = MastodonWordFilter.Id(
                    accountId = 0,
                    filterId = ""
                ),
                phrase = "",
                context = listOf(MastodonWordFilter.FilterContext.Home),
                wholeWord = false,
                expiresAt = null,
                irreversible = false
            ),
            MastodonWordFilter(
                id = MastodonWordFilter.Id(
                    accountId = 0,
                    filterId = ""
                ),
                phrase = "",
                context = listOf(
                    MastodonWordFilter.FilterContext.Home,
                    MastodonWordFilter.FilterContext.Public
                ),
                wholeWord = false,
                expiresAt = null,
                irreversible = false
            )
        )
        val filters = listOf(
            MastodonWordFilter(
                id = MastodonWordFilter.Id(
                    accountId = 0,
                    filterId = ""
                ),
                phrase = "",
                context = listOf(
                    MastodonWordFilter.FilterContext.Account,
                    MastodonWordFilter.FilterContext.Thread,
                    MastodonWordFilter.FilterContext.Notifications,
                    MastodonWordFilter.FilterContext.Public
                ),
                wholeWord = false,
                expiresAt = null,
                irreversible = false
            ),
        ) + expected
        val result = GetMatchContextFilters().invoke(
            Pageable.Mastodon.HomeTimeline,
            filters
        )
        assertEquals(expected, result)
    }

    @Test
    fun giveHasPublicContextAndPublicPageable() {

        val expected = listOf(
            MastodonWordFilter(
                id = MastodonWordFilter.Id(
                    accountId = 0,
                    filterId = ""
                ),
                phrase = "",
                context = listOf(MastodonWordFilter.FilterContext.Public),
                wholeWord = false,
                expiresAt = null,
                irreversible = false
            ),
            MastodonWordFilter(
                id = MastodonWordFilter.Id(
                    accountId = 0,
                    filterId = ""
                ),
                phrase = "",
                context = listOf(
                    MastodonWordFilter.FilterContext.Public,
                    MastodonWordFilter.FilterContext.Thread
                ),
                wholeWord = false,
                expiresAt = null,
                irreversible = false
            )
        )
        val filters = listOf(
            MastodonWordFilter(
                id = MastodonWordFilter.Id(
                    accountId = 0,
                    filterId = ""
                ),
                phrase = "",
                context = listOf(
                    MastodonWordFilter.FilterContext.Account,
                    MastodonWordFilter.FilterContext.Thread,
                    MastodonWordFilter.FilterContext.Notifications,
                    MastodonWordFilter.FilterContext.Home
                ),
                wholeWord = false,
                expiresAt = null,
                irreversible = false
            ),
        ) + expected
        val result = GetMatchContextFilters().invoke(
            Pageable.Mastodon.PublicTimeline(),
            filters
        )
        assertEquals(expected, result)
    }

    @Test
    fun giveHasPublicContextAndLocalOnlyPageable() {

        val expected = listOf(
            MastodonWordFilter(
                id = MastodonWordFilter.Id(
                    accountId = 0,
                    filterId = ""
                ),
                phrase = "",
                context = listOf(MastodonWordFilter.FilterContext.Public),
                wholeWord = false,
                expiresAt = null,
                irreversible = false
            ),
            MastodonWordFilter(
                id = MastodonWordFilter.Id(
                    accountId = 0,
                    filterId = ""
                ),
                phrase = "",
                context = listOf(
                    MastodonWordFilter.FilterContext.Public,
                    MastodonWordFilter.FilterContext.Thread
                ),
                wholeWord = false,
                expiresAt = null,
                irreversible = false
            )
        )
        val filters = listOf(
            MastodonWordFilter(
                id = MastodonWordFilter.Id(
                    accountId = 0,
                    filterId = ""
                ),
                phrase = "",
                context = listOf(
                    MastodonWordFilter.FilterContext.Account,
                    MastodonWordFilter.FilterContext.Thread,
                    MastodonWordFilter.FilterContext.Notifications,
                    MastodonWordFilter.FilterContext.Home
                ),
                wholeWord = false,
                expiresAt = null,
                irreversible = false
            ),
        ) + expected
        val result = GetMatchContextFilters().invoke(
            Pageable.Mastodon.LocalTimeline(),
            filters
        )
        assertEquals(expected, result)
    }
}