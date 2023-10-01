package net.pantasystem.milktea.model.filter

import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.make
import net.pantasystem.milktea.model.note.poll.Poll
import net.pantasystem.milktea.model.user.User
import org.junit.Assert
import org.junit.Test


class MastodonFilterServiceTest {

    @Test
    fun isShouldFilterNote_GiveMatchText() {
        val service = MastodonFilterService(
            FilterPatternCache(),
            GetMatchContextFilters()
        )
        val actual = service.isShouldFilterNote(
            Pageable.Mastodon.HomeTimeline(),
            filters = listOf(
                MastodonWordFilter(
                    id = MastodonWordFilter.Id(
                        accountId = 0,
                        filterId = ""
                    ),
                    phrase = "piyo",
                    context = listOf(
                        MastodonWordFilter.FilterContext.Home
                    ),
                    wholeWord = false,
                    expiresAt = null,
                    irreversible = false
                )
            ),
            note = Note.make(
                Note.Id(0L, ""),
                User.Id(0L, ""),
                text = "hogepiyofuga"
            )
        )
        Assert.assertTrue(actual)
    }

    @Test
    fun isShouldFilterNote_GiveUnMatchText() {
        val service = MastodonFilterService(
            FilterPatternCache(),
            GetMatchContextFilters()
        )
        val actual = service.isShouldFilterNote(
            Pageable.Mastodon.HomeTimeline(),
            filters = listOf(
                MastodonWordFilter(
                    id = MastodonWordFilter.Id(
                        accountId = 0,
                        filterId = ""
                    ),
                    phrase = "piyo",
                    context = listOf(
                        MastodonWordFilter.FilterContext.Home
                    ),
                    wholeWord = false,
                    expiresAt = null,
                    irreversible = false
                )
            ),
            note = Note.make(
                Note.Id(0L, ""),
                User.Id(0L, ""),
                text = "piyutarou"
            )
        )
        Assert.assertFalse(actual)
    }

    @Test
    fun isShouldFilterNote_GiveMatchTextInPoll() {
        val service = MastodonFilterService(
            FilterPatternCache(),
            GetMatchContextFilters()
        )
        val actual = service.isShouldFilterNote(
            Pageable.Mastodon.HomeTimeline(),
            filters = listOf(
                MastodonWordFilter(
                    id = MastodonWordFilter.Id(
                        accountId = 0,
                        filterId = ""
                    ),
                    phrase = "piyo",
                    context = listOf(
                        MastodonWordFilter.FilterContext.Home
                    ),
                    wholeWord = false,
                    expiresAt = null,
                    irreversible = false
                )
            ),
            note = Note.make(
                Note.Id(0L, ""),
                User.Id(0L, ""),
                text = "piyutarou",
                poll = Poll(
                    choices = listOf(
                        Poll.Choice(
                            0,
                            "piyo",
                            10,
                            false,
                        ),
                        Poll.Choice(
                            1,
                            "hoge",
                            10,
                            false,
                        ),
                        Poll.Choice(
                            2,
                            "fuga",
                            10,
                            false,
                        ),
                    ),
                    null,
                    false,
                )
            )
        )
        Assert.assertTrue(actual)
    }

    @Test
    fun isShouldFilterNote_GiveOtherContext() {
        val service = MastodonFilterService(
            FilterPatternCache(),
            GetMatchContextFilters()
        )
        val actual = service.isShouldFilterNote(
            Pageable.Mastodon.HomeTimeline(),
            filters = listOf(
                MastodonWordFilter(
                    id = MastodonWordFilter.Id(
                        accountId = 0,
                        filterId = ""
                    ),
                    phrase = "piyo",
                    context = listOf(
                        MastodonWordFilter.FilterContext.Public
                    ),
                    wholeWord = false,
                    expiresAt = null,
                    irreversible = false
                ),
                MastodonWordFilter(
                    id = MastodonWordFilter.Id(
                        accountId = 0,
                        filterId = ""
                    ),
                    phrase = "fuga",
                    context = listOf(
                        MastodonWordFilter.FilterContext.Public
                    ),
                    wholeWord = false,
                    expiresAt = null,
                    irreversible = false
                )
            ),
            note = Note.make(
                Note.Id(0L, ""),
                User.Id(0L, ""),
                text = "piyopiyo",
                poll = Poll(
                    choices = listOf(
                        Poll.Choice(
                            0,
                            "piyo",
                            10,
                            false,
                        ),
                        Poll.Choice(
                            1,
                            "hoge",
                            10,
                            false,
                        ),
                        Poll.Choice(
                            2,
                            "fuga",
                            10,
                            false,
                        ),
                    ),
                    null,
                    false,
                )
            )
        )
        Assert.assertFalse(actual)
    }

    @Test
    fun isShouldFilterNote_GiveManyFilters() {
        val service = MastodonFilterService(
            FilterPatternCache(),
            GetMatchContextFilters()
        )
        val actual = service.isShouldFilterNote(
            Pageable.Mastodon.HomeTimeline(),
            filters = listOf(
                MastodonWordFilter(
                    id = MastodonWordFilter.Id(
                        accountId = 0,
                        filterId = ""
                    ),
                    phrase = "piyo",
                    context = listOf(
                        MastodonWordFilter.FilterContext.Home
                    ),
                    wholeWord = false,
                    expiresAt = null,
                    irreversible = false
                ),
                MastodonWordFilter(
                    id = MastodonWordFilter.Id(
                        accountId = 0,
                        filterId = ""
                    ),
                    phrase = "fuga",
                    context = listOf(
                        MastodonWordFilter.FilterContext.Home
                    ),
                    wholeWord = false,
                    expiresAt = null,
                    irreversible = false
                ),
                MastodonWordFilter(
                    id = MastodonWordFilter.Id(
                        accountId = 0,
                        filterId = ""
                    ),
                    phrase = "hoge",
                    context = listOf(
                        MastodonWordFilter.FilterContext.Home
                    ),
                    wholeWord = false,
                    expiresAt = null,
                    irreversible = false
                )
            ),
            note = Note.make(
                Note.Id(0L, ""),
                User.Id(0L, ""),
                text = "piyofugahoge"
            )
        )
        Assert.assertTrue(actual)
    }

    @Test
    fun isShouldFilterNote_GiveManyFiltersAndNotMatchText() {
        val service = MastodonFilterService(
            FilterPatternCache(),
            GetMatchContextFilters()
        )
        val actual = service.isShouldFilterNote(
            Pageable.Mastodon.HomeTimeline(),
            filters = listOf(
                MastodonWordFilter(
                    id = MastodonWordFilter.Id(
                        accountId = 0,
                        filterId = ""
                    ),
                    phrase = "piyo",
                    context = listOf(
                        MastodonWordFilter.FilterContext.Home
                    ),
                    wholeWord = false,
                    expiresAt = null,
                    irreversible = false
                ),
                MastodonWordFilter(
                    id = MastodonWordFilter.Id(
                        accountId = 0,
                        filterId = ""
                    ),
                    phrase = "fuga",
                    context = listOf(
                        MastodonWordFilter.FilterContext.Home
                    ),
                    wholeWord = false,
                    expiresAt = null,
                    irreversible = false
                ),
                MastodonWordFilter(
                    id = MastodonWordFilter.Id(
                        accountId = 0,
                        filterId = ""
                    ),
                    phrase = "hoge",
                    context = listOf(
                        MastodonWordFilter.FilterContext.Home
                    ),
                    wholeWord = false,
                    expiresAt = null,
                    irreversible = false
                )
            ),
            note = Note.make(
                Note.Id(0L, ""),
                User.Id(0L, ""),
                text = "kawaiipantaharunonmeltazuki"
            )
        )
        Assert.assertFalse(actual)
    }

    @Test
    fun isShouldFilterNote_GiveWholeWord() {
        val service = MastodonFilterService(
            FilterPatternCache(),
            GetMatchContextFilters()
        )
        val actual = service.isShouldFilterNote(
            Pageable.Mastodon.HomeTimeline(),
            filters = listOf(
                MastodonWordFilter(
                    id = MastodonWordFilter.Id(
                        accountId = 0,
                        filterId = ""
                    ),
                    phrase = "piyo",
                    context = listOf(
                        MastodonWordFilter.FilterContext.Home
                    ),
                    wholeWord = true,
                    expiresAt = null,
                    irreversible = false
                )
            ),
            note = Note.make(
                Note.Id(0L, ""),
                User.Id(0L, ""),
                text = "hoge piyo fuga"
            )
        )
        Assert.assertTrue(actual)
    }

    @Test
    fun isShouldFilterNote_GiveWholeWordNotMatched() {
        val service = MastodonFilterService(
            FilterPatternCache(),
            GetMatchContextFilters()
        )
        val actual = service.isShouldFilterNote(
            Pageable.Mastodon.HomeTimeline(),
            filters = listOf(
                MastodonWordFilter(
                    id = MastodonWordFilter.Id(
                        accountId = 0,
                        filterId = ""
                    ),
                    phrase = "piyo",
                    context = listOf(
                        MastodonWordFilter.FilterContext.Home
                    ),
                    wholeWord = true,
                    expiresAt = null,
                    irreversible = false
                )
            ),
            note = Note.make(
                Note.Id(0L, ""),
                User.Id(0L, ""),
                text = "hogepiyofuga"
            )
        )
        Assert.assertFalse(actual)
    }
}