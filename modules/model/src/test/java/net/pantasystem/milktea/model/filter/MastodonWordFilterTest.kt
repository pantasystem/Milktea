package net.pantasystem.milktea.model.filter

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class MastodonWordFilterTest {
    private val blankFilter = MastodonWordFilter(
        id = MastodonWordFilter.Id(
            accountId = 0,
            filterId = ""
        ),
        phrase = "",
        context = listOf(),
        wholeWord = false,
        expiresAt = null,
        irreversible = false
    )

    @Test
    fun isContextHome_GiveNoHomeContextReturnsFalse() {
        val filter = blankFilter.copy(
            context = listOf(
                MastodonWordFilter.FilterContext.Public
            )
        )
        assertFalse(filter.isContextHome)
    }

    @Test
    fun isContextHome_GiveHomeContextReturnsTrue() {
        val filter = blankFilter.copy(
            context = listOf(
                MastodonWordFilter.FilterContext.Home
            )
        )
        assertTrue(filter.isContextHome)
    }

    @Test
    fun isContextPublic_GivePublicContextReturnsTrue() {
        val filter = blankFilter.copy(
            context = listOf(
                MastodonWordFilter.FilterContext.Public,
                MastodonWordFilter.FilterContext.Home
            )
        )
        assertTrue(filter.isContextPublic)
    }

    @Test
    fun isContextPublic_GiveNoPublicContextReturnsFalse() {
        val filter = blankFilter.copy(
            context = listOf(
                MastodonWordFilter.FilterContext.Home,
                MastodonWordFilter.FilterContext.Notifications
            )
        )
        assertFalse(filter.isContextPublic)
    }

    @Test
    fun isContextNotifications_GiveNotificationsContextReturnsTrue() {
        val filter = blankFilter.copy(
            context = listOf(
                MastodonWordFilter.FilterContext.Notifications
            )
        )
        assertTrue(filter.isContextNotifications)
    }

    @Test
    fun isContextNotifications_GiveNoNotificationsContextReturnsFalse() {
        val filter = blankFilter.copy(
            context = listOf(
                MastodonWordFilter.FilterContext.Home
            )
        )
        assertFalse(filter.isContextNotifications)
    }

    @Test
    fun isContextAccount_GiveNoAccountContextReturnsFalse() {
        val filter = blankFilter.copy(
            context = listOf(
                MastodonWordFilter.FilterContext.Home
            )
        )
        assertFalse(filter.isContextAccount)
    }

    @Test
    fun isContextAccount_GiveAccountContextReturnsTrue() {
        val filter = blankFilter.copy(
            context = listOf(
                MastodonWordFilter.FilterContext.Account
            )
        )
        assertTrue(filter.isContextAccount)
    }

    @Test
    fun isContextThread_GiveThreadContextReturnsTrue() {
        val filter = blankFilter.copy(
            context = listOf(
                MastodonWordFilter.FilterContext.Thread
            )
        )
        assertTrue(filter.isContextThread)
    }

    @Test
    fun isContextThread_GiveNoThreadContextReturnsFalse() {
        val filter = blankFilter.copy(
            context = listOf(
                MastodonWordFilter.FilterContext.Account
            )
        )
        assertFalse(filter.isContextThread)
    }


}