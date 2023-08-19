package net.pantasystem.milktea.model.note

import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.note.poll.Poll
import net.pantasystem.milktea.model.note.reaction.ReactionCount
import net.pantasystem.milktea.model.user.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


class NoteTest {

    @Test
    fun isQuote() {
        val note = generateEmptyNote()
        val quoteNote = note.copy(
            renoteId = Note.Id(0L, "noteId"),
            text = "text"
        )

        assertTrue(quoteNote.isQuote())
    }


    @Test
    fun hasContent_OnlyText() {
        assertTrue(generateEmptyNote().copy(text = "text").hasContent())
    }

    @Test
    fun hasContent_OnlyFiles() {
        assertTrue(generateEmptyNote().copy(fileIds = listOf(FileProperty.Id(0L, ""))).hasContent())
    }

    @Test
    fun hasContent_OnlyPoll() {
        val note = generateEmptyNote().copy(
            poll = Poll(choices = emptyList(), multiple = false, expiresAt = null)
        )
        assertTrue(note.hasContent())
    }

    @Test
    fun hasContent_ReturnsFalse() {
        // NOTE: cwだけの場合はcw扱いにならない
        val note = generateEmptyNote().copy(cw = "a")
        assertFalse(note.hasContent())
    }

    @Test
    fun canRenote_WhenVisibilityPublic() {
        val note = generateEmptyNote().copy(
            visibility = Visibility.Public(true)
        )
        assertTrue(note.canRenote(User.Id(accountId = note.id.accountId, "acId")))
    }

    @Test
    fun canRenote_WhenVisibilityHome() {
        val note = generateEmptyNote().copy(
            visibility = Visibility.Home(true)
        )
        assertTrue(note.canRenote(User.Id(accountId = note.id.accountId, "acId")))
    }

    @Test
    fun canRenote_WhenVisibilityFollowersReturnFalse() {
        val note = generateEmptyNote().copy(
            visibility = Visibility.Followers(false),
        )
        assertFalse(note.canRenote(User.Id(accountId = note.id.accountId, "acId")))
    }

    @Test
    fun canRenote_WhenVisibilitySpecifiedReturnFalse() {
        val note = generateEmptyNote().copy(
            visibility = Visibility.Specified(emptyList()),
        )
        assertFalse(note.canRenote(User.Id(accountId = note.id.accountId, "acId")))
    }

    @Test
    fun canRenote_WhenVisibilityFollowersAndMyPostReturnTrue() {
        val note = generateEmptyNote().copy(
            visibility = Visibility.Followers(false),
        )
        assertTrue(note.canRenote(note.userId))
    }

    @Test
    fun canRenote_WhenVisibilitySpecifiedAndMyNote() {
        val userId = generateEmptyNote().userId
        val note = generateEmptyNote().copy(
            visibility = Visibility.Specified(emptyList()),
            userId = userId,
        )
        assertTrue(note.canRenote(userId))
    }

    @Test
    fun canRenote_GetByOtherAccountId() {
        val note = generateEmptyNote().copy(
            visibility = Visibility.Public(false),
        )
        assertFalse(note.canRenote(User.Id(accountId = note.id.accountId + 1L, "acId")))
    }

    @Test
    fun isQuote_GiveFedibirdPost() {
        val note = generateEmptyNote().copy(
            text = "<p>hogehoge</p>",
            type = Note.Type.Mastodon(
                reblogged = null,
                favorited = null,
                bookmarked = null,
                muted = null,
                favoriteCount = null,
                tags = listOf(),
                mentions = listOf(),
                isFedibirdQuote = true,
                pollId = null,
                isSensitive = null,
                pureText = null,
                isReactionAvailable = true,
            )
        )
        assertTrue(note.isQuote())
    }

    @Test
    fun isQuote_GiveMastodonBoostReturnsFalse() {
        val note = generateEmptyNote().copy(
            text = "<p>hogehoge</p>",
            type = Note.Type.Mastodon(
                reblogged = null,
                favorited = null,
                bookmarked = null,
                muted = null,
                favoriteCount = null,
                tags = listOf(),
                mentions = listOf(),
                isFedibirdQuote = false,
                pollId = null,
                isSensitive = null,
                pureText = null,
                isReactionAvailable = true,
            )
        )
        assertFalse(note.isQuote())
    }

    @Test
    fun isRenote_GiveMastodonBoost() {
        val note = generateEmptyNote().copy(
            text = "<p>hogehoge</p>",
            type = Note.Type.Mastodon(
                reblogged = null,
                favorited = null,
                bookmarked = null,
                muted = null,
                favoriteCount = null,
                tags = listOf(),
                mentions = listOf(),
                isFedibirdQuote = false,
                pollId = null,
                isSensitive = null,
                pureText = null,
                isReactionAvailable = true,
            ),
            renoteId = Note.Id(0L, "id")
        )
        assertTrue(note.isRenote())
    }

    @Test
    fun hasContent_GiveMastodonBoostReturnsFalse() {
        val note = generateEmptyNote().copy(
            text = "<p>hogehoge</p>",
            renoteId = Note.Id(0L, "id"),
            type = Note.Type.Mastodon(
                reblogged = null,
                favorited = null,
                bookmarked = null,
                muted = null,
                favoriteCount = null,
                tags = listOf(),
                mentions = listOf(),
                isFedibirdQuote = false,
                pollId = null,
                isSensitive = null,
                pureText = null,
                isReactionAvailable = true,
            )
        )
        assertFalse(note.hasContent())
    }

    @Test
    fun shortReactionCounts_GiveOverMaxCountCounts() {
        val counts = (0..(Note.SHORT_REACTION_COUNT_MAX_SIZE)).map {
            ReactionCount("r$it", 1, false)
        }
        val note = Note.make(
            id = Note.Id(0L, ""),
            text = null,
            userId = User.Id(0L, ""),
            reactionCounts = counts,
        )
        assertEquals(Note.SHORT_REACTION_COUNT_MAX_SIZE + 1, counts.size)
        assertEquals(Note.SHORT_REACTION_COUNT_MAX_SIZE, note.getShortReactionCounts(false).size)
        assertEquals(counts.subList(0, Note.SHORT_REACTION_COUNT_MAX_SIZE), note.getShortReactionCounts(false))
    }

    @Test
    fun shortReactionCounts_GiveUnderMaxCountCounts() {
        val counts = (0 until (Note.SHORT_REACTION_COUNT_MAX_SIZE - 1)).map {
            ReactionCount("r$it", 1, true)
        }
        val note = Note.make(
            id = Note.Id(0L, ""),
            text = null,
            userId = User.Id(0L, ""),
            reactionCounts = counts,
        )
        assertEquals(Note.SHORT_REACTION_COUNT_MAX_SIZE - 1, counts.size)
        assertEquals(Note.SHORT_REACTION_COUNT_MAX_SIZE - 1, note.getShortReactionCounts(false).size)
        assertEquals(counts, note.getShortReactionCounts(false))
    }

    @Test
    fun shortReactionCounts_GiveMaxCountCounts() {
        val counts = (0 until Note.SHORT_REACTION_COUNT_MAX_SIZE).map {
            ReactionCount("r$it", 1, false)
        }
        val note = Note.make(
            id = Note.Id(0L, ""),
            text = null,
            userId = User.Id(0L, ""),
            reactionCounts = counts,
        )
        assertEquals(Note.SHORT_REACTION_COUNT_MAX_SIZE, counts.size)
        assertEquals(Note.SHORT_REACTION_COUNT_MAX_SIZE, note.getShortReactionCounts(false).size, )
        assertEquals(counts, note.getShortReactionCounts(false))
    }

    @Test
    fun shortReactionCounts_GiveZeroElementsCounts() {

        val note = Note.make(
            id = Note.Id(0L, ""),
            text = null,
            userId = User.Id(0L, ""),
            reactionCounts = emptyList(),
        )
        assertEquals(emptyList<ReactionCount>(), note.getShortReactionCounts(false))
    }

    @Test
    fun shortReactionCounts_GiveRenoteAndMaxCounts() {
        val counts = (0 until Note.SHORT_RENOTE_REACTION_COUNT_MAX_SIZE).map {
            ReactionCount("r$it", 1, false)
        }
        val note = Note.make(
            id = Note.Id(0L, ""),
            text = null,
            userId = User.Id(0L, ""),
            reactionCounts = counts,
            renoteId = Note.Id(0L, "")
        )
        assertEquals(Note.SHORT_RENOTE_REACTION_COUNT_MAX_SIZE, counts.size)
        assertEquals(Note.SHORT_RENOTE_REACTION_COUNT_MAX_SIZE, note.getShortReactionCounts(true).size)
        assertEquals(counts, note.getShortReactionCounts(true))
    }

    @Test
    fun shortReactionCounts_GiveRenoteAndZeroElementsCounts() {
        val note = Note.make(
            id = Note.Id(0L, ""),
            text = null,
            userId = User.Id(0L, ""),
            reactionCounts = emptyList(),
            renoteId = Note.Id(0L, "")
        )
        assertEquals(emptyList<ReactionCount>(), note.getShortReactionCounts(true))
    }

    @Test
    fun shortReactionCounts_GiveRenoteAndUnderMaxCountCounts() {
        val counts = (0 until (Note.SHORT_RENOTE_REACTION_COUNT_MAX_SIZE - 1)).map {
            ReactionCount("r$it", 1, false)
        }
        val note = Note.make(
            id = Note.Id(0L, ""),
            text = null,
            userId = User.Id(0L, ""),
            reactionCounts = counts,
            renoteId = Note.Id(0L, "")
        )
        assertEquals(Note.SHORT_RENOTE_REACTION_COUNT_MAX_SIZE - 1, counts.size)
        assertEquals(Note.SHORT_RENOTE_REACTION_COUNT_MAX_SIZE - 1, note.getShortReactionCounts(true).size)
        assertEquals(counts, note.getShortReactionCounts(true))
    }

    @Test
    fun shortReactionCounts_GiveRenoteAndOverMaxCountCounts() {
        val counts = (0..(Note.SHORT_RENOTE_REACTION_COUNT_MAX_SIZE)).map {
            ReactionCount("r$it", 1, false)
        }
        val note = Note.make(
            id = Note.Id(0L, ""),
            text = null,
            userId = User.Id(0L, ""),
            reactionCounts = counts,
            renoteId = Note.Id(0L, "")
        )
        assertEquals(Note.SHORT_RENOTE_REACTION_COUNT_MAX_SIZE + 1, counts.size)
        assertEquals(Note.SHORT_RENOTE_REACTION_COUNT_MAX_SIZE, note.getShortReactionCounts(true).size)
        assertEquals(counts.subList(0, Note.SHORT_RENOTE_REACTION_COUNT_MAX_SIZE), note.getShortReactionCounts(true))
    }

    @Test
    fun isReactedReaction_GiveReactedReaction() {
        val note = Note.make(
            id = Note.Id(0L, ""),
            text = null,
            userId = User.Id(0L, ""),
            reactionCounts = listOf(
                ReactionCount(
                    ":kawaii:",
                    1,
                    true,
                ),
                ReactionCount(
                    ":iizo:",
                    1,
                    false,
                )
            ),
            renoteId = Note.Id(0L, "")
        )

        assertFalse(note.isReactedReaction(":iizo:"))
        assertTrue(note.isReactedReaction(":kawaii:"))
    }

    @Test
    fun getMyReactionCount_GiveAnyReactedReactions() {
        val note = Note.make(
            id = Note.Id(0L, ""),
            text = null,
            userId = User.Id(0L, ""),
            reactionCounts = listOf(
                ReactionCount(
                    ":kawaii:",
                    1,
                    true,
                ),
                ReactionCount(
                    ":iizo:",
                    1,
                    true,
                ),
                ReactionCount(
                    ":dame:",
                    1,
                    false,
                ),
                ReactionCount(
                    ":souiuhimoaru:",
                    1,
                    false,
                ),
                ReactionCount(
                    ":angry:",
                    1,
                    false,
                )
            ),
            renoteId = Note.Id(0L, "")
        )
        assertEquals(2, note.getMyReactionCount())
    }


}