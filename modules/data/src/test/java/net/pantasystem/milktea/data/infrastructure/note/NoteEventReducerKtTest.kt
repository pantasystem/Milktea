package net.pantasystem.milktea.data.infrastructure.note

import net.pantasystem.milktea.api.misskey.emoji.CustomEmojiNetworkDTO
import net.pantasystem.milktea.api_streaming.NoteUpdated
import net.pantasystem.milktea.api_streaming.mastodon.EmojiReaction
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.emoji.CustomEmoji
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.make
import net.pantasystem.milktea.model.note.reaction.ReactionCount
import net.pantasystem.milktea.model.user.User
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


class NoteEventReducerKtTest {
    private val account = Account("test", "misskey.io", "Panta", Account.InstanceType.MISSKEY, "")

    @Test
    fun onReacted_GiveOtherUserReaction() {
        val note = Note.make(
            id = Note.Id(0L, "1"),
            text = "",
            userId = User.Id(0L, "2")
        )

        val result = note.onReacted(
            account,
            NoteUpdated.Body.Reacted(
                id = "1",
                body = NoteUpdated.Body.Reacted.Body(
                    reaction = ":kawaii:",
                    userId = "other"
                )
            ),
            null,
            null
        )

        Assertions.assertEquals(listOf(ReactionCount(":kawaii:", 1, false)), result.reactionCounts)
        Assertions.assertNull(result.myReaction)
    }

    @Test
    fun onReacted_GiveMyReaction() {
        val note = Note.make(
            id = Note.Id(0L, "1"),
            text = "",
            userId = User.Id(0L, "2")
        )

        val result = note.onReacted(
            account,
            NoteUpdated.Body.Reacted(
                id = "1",
                body = NoteUpdated.Body.Reacted.Body(
                    reaction = ":kawaii:",
                    userId = account.remoteId
                )
            ),
            null,
            null
        )

        Assertions.assertEquals(listOf(ReactionCount(":kawaii:", 1, true)), result.reactionCounts)
        Assertions.assertEquals(":kawaii:", result.myReaction)
    }

    @Test
    fun onReacted_GiveMyReactionAndHasReactions() {
        val note = Note.make(
            id = Note.Id(0L, "1"),
            text = "",
            userId = User.Id(0L, "2"),
            reactionCounts = listOf(
                ReactionCount(":watasimo:", 1, false),
                ReactionCount(":kawaii:", 1, false)
            )
        )

        val result = note.onReacted(
            account, NoteUpdated.Body.Reacted(
                id = "1",
                body = NoteUpdated.Body.Reacted.Body(
                    reaction = ":kawaii:",
                    userId = account.remoteId
                )
            ),
            null,
            null

        )

        Assertions.assertEquals(
            listOf(ReactionCount(":watasimo:", 1, false), ReactionCount(":kawaii:", 2, true)),
            result.reactionCounts
        )
        Assertions.assertEquals(":kawaii:", result.myReaction)
    }

    @Test
    fun onReacted_GiveMyReactionAndHasOtherReactions() {
        val note = Note.make(
            id = Note.Id(0L, "1"),
            text = "",
            userId = User.Id(0L, "2"),
            reactionCounts = listOf(
                ReactionCount(":watasimo:", 1, false),
            )
        )

        val result = note.onReacted(
            account, NoteUpdated.Body.Reacted(
                id = "1",
                body = NoteUpdated.Body.Reacted.Body(
                    reaction = ":kawaii:",
                    userId = account.remoteId
                )
            ),
            null,
            null
        )

        Assertions.assertEquals(
            listOf(ReactionCount(":watasimo:", 1, false), ReactionCount(":kawaii:", 1, true)),
            result.reactionCounts
        )
        Assertions.assertEquals(":kawaii:", result.myReaction)
    }

    @Test
    fun onUnReacted() {
        val note = Note.make(
            id = Note.Id(0L, "1"),
            text = "",
            userId = User.Id(0L, "2"),
            reactionCounts = listOf(
                ReactionCount(":watasimo:", 1, false),
            )
        )
        val result = note.onUnReacted(
            account, NoteUpdated.Body.Unreacted(
                note.id.noteId, NoteUpdated.Body.Unreacted.Body(
                    ":watasimo:",
                    userId = "other"
                )
            )
        )
        Assertions.assertNull(result.myReaction)
        Assertions.assertEquals(emptyList<ReactionCount>(), result.reactionCounts)
    }

    @Test
    fun onUnReacted_GiveMyEvent() {
        val note = Note.make(
            id = Note.Id(0L, "1"),
            text = "",
            userId = User.Id(0L, "2"),
            reactionCounts = listOf(
                ReactionCount(":watasimo:", 1, true),
            ),
            myReaction = ":watasimo:"
        )
        val result = note.onUnReacted(
            account, NoteUpdated.Body.Unreacted(
                note.id.noteId, NoteUpdated.Body.Unreacted.Body(
                    ":watasimo:",
                    userId = account.remoteId
                )
            )
        )
        Assertions.assertNull(result.myReaction)
        Assertions.assertEquals(emptyList<ReactionCount>(), result.reactionCounts)
    }

    @Test
    fun onUnReacted_GiveOtherUserEvent() {
        val note = Note.make(
            id = Note.Id(0L, "1"),
            text = "",
            userId = User.Id(0L, "2"),
            reactionCounts = listOf(
                ReactionCount(":watasimo:", 2, true),
            ),
            myReaction = ":watasimo:"
        )
        val result = note.onUnReacted(
            account, NoteUpdated.Body.Unreacted(
                note.id.noteId, NoteUpdated.Body.Unreacted.Body(
                    ":watasimo:",
                    userId = "other"
                )
            )
        )
        Assertions.assertEquals(":watasimo:", result.myReaction)
        Assertions.assertEquals(
            listOf(
                ReactionCount(":watasimo:", 1, true),
            ), result.reactionCounts
        )
    }

    @Test
    fun onReacted_GiveNewEmoji() {
        val note = Note.make(
            id = Note.Id(0L, "1"),
            text = "",
            userId = User.Id(0L, "2"),
            reactionCounts = listOf(
                ReactionCount(":watasimo:", 2, true),
            ),
            myReaction = ":watasimo:"
        )
        val result = note.onReacted(
            account,
            NoteUpdated.Body.Reacted(
                id = "1",
                body = NoteUpdated.Body.Reacted.Body(
                    reaction = ":kawaii:",
                    userId = account.remoteId,
                    emoji = CustomEmojiNetworkDTO(
                        name = ":kawaii:"
                    )
                )
            ),
            null,
            null
        )
        Assertions.assertEquals(
            listOf(
                CustomEmoji(
                    name = ":kawaii:"
                )
            ), result.emojis
        )
    }


    @Test
    fun onEmojiReacted_GiveAddMyReaction() {
        val note = Note.make(
            id = Note.Id(0L, "1"),
            text = "",
            userId = User.Id(0L, "2"),
            reactionCounts = listOf(
                ReactionCount("watasimo", 1, false),
            ),
            myReaction = null
        )
        val updated = note.onEmojiReacted(
            account, EmojiReaction(
                name = "watasimo",
                count = 2,
                url = null,
                staticUrl = null,
                domain = null,
                accountIds = listOf("test"),
                statusId = "1"
            ),
            null
        )
        Assertions.assertEquals("watasimo", updated.myReaction)
        Assertions.assertEquals(updated.reactionCounts, listOf(
            ReactionCount("watasimo", 2, true)
        ))
    }

    @Test
    fun onEmojiReacted_Unreacted() {
        val note = Note.make(
            id = Note.Id(0L, "1"),
            text = "",
            userId = User.Id(0L, "2"),
            reactionCounts = listOf(
                ReactionCount("watasimo", 2, true),
            ),
            myReaction = "watasimo"
        )
        val updated = note.onEmojiReacted(
            account, EmojiReaction(
                name = "watasimo",
                count = 1,
                url = null,
                staticUrl = null,
                domain = null,
                accountIds = listOf(),
                statusId = "1"
            ),
            null
        )
        Assertions.assertEquals(null, updated.myReaction)
        Assertions.assertEquals(updated.reactionCounts, listOf(
            ReactionCount("watasimo", 1, false)
        ))
    }

    @Test
    fun onEmojiReacted_WhenApplied() {
        val note = Note.make(
            id = Note.Id(0L, "1"),
            text = "",
            userId = User.Id(0L, "2"),
            reactionCounts = listOf(
                ReactionCount("watasimo", 2, true),
            ),
            myReaction = "watasimo"
        )
        val updated = note.onEmojiReacted(
            account, EmojiReaction(
                name = "watasimo",
                count = 2,
                url = null,
                staticUrl = null,
                domain = null,
                accountIds = listOf("test"),
                statusId = "1"
            ),
            null
        )
        Assertions.assertEquals("watasimo", updated.myReaction)
        Assertions.assertEquals(updated.reactionCounts, listOf(
            ReactionCount("watasimo", 2, true)
        ))
    }
}