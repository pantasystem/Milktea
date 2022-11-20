package net.pantasystem.milktea.data.infrastructure.notes

import net.pantasystem.milktea.api_streaming.NoteUpdated
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.make
import net.pantasystem.milktea.model.notes.reaction.ReactionCount
import net.pantasystem.milktea.model.user.User
import org.junit.Assert
import org.junit.Test

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
            )
        )

        Assert.assertEquals(listOf(ReactionCount(":kawaii:", 1)), result.reactionCounts)
        Assert.assertNull(result.myReaction)
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
            )
        )

        Assert.assertEquals(listOf(ReactionCount(":kawaii:", 1)), result.reactionCounts)
        Assert.assertEquals(":kawaii:", result.myReaction)
    }

    @Test
    fun onReacted_GiveMyReactionAndHasReactions() {
        val note = Note.make(
            id = Note.Id(0L, "1"),
            text = "",
            userId = User.Id(0L, "2"),
            reactionCounts = listOf(
                ReactionCount(":watasimo:", 1),
                ReactionCount(":kawaii:", 1)
            )
        )

        val result = note.onReacted(
            account, NoteUpdated.Body.Reacted(
                id = "1",
                body = NoteUpdated.Body.Reacted.Body(
                    reaction = ":kawaii:",
                    userId = account.remoteId
                )
            )
        )

        Assert.assertEquals(
            listOf(ReactionCount(":watasimo:", 1), ReactionCount(":kawaii:", 2)),
            result.reactionCounts
        )
        Assert.assertEquals(":kawaii:", result.myReaction)
    }

    @Test
    fun onReacted_GiveMyReactionAndHasOtherReactions() {
        val note = Note.make(
            id = Note.Id(0L, "1"),
            text = "",
            userId = User.Id(0L, "2"),
            reactionCounts = listOf(
                ReactionCount(":watasimo:", 1),
            )
        )

        val result = note.onReacted(
            account, NoteUpdated.Body.Reacted(
                id = "1",
                body = NoteUpdated.Body.Reacted.Body(
                    reaction = ":kawaii:",
                    userId = account.remoteId
                )
            )
        )

        Assert.assertEquals(
            listOf(ReactionCount(":watasimo:", 1), ReactionCount(":kawaii:", 1)),
            result.reactionCounts
        )
        Assert.assertEquals(":kawaii:", result.myReaction)
    }

    @Test
    fun onUnReacted() {
        val note = Note.make(
            id = Note.Id(0L, "1"),
            text = "",
            userId = User.Id(0L, "2"),
            reactionCounts = listOf(
                ReactionCount(":watasimo:", 1),
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
        Assert.assertNull(result.myReaction)
        Assert.assertEquals(emptyList<ReactionCount>(), result.reactionCounts)
    }

    @Test
    fun onUnReacted_GiveMyEvent() {
        val note = Note.make(
            id = Note.Id(0L, "1"),
            text = "",
            userId = User.Id(0L, "2"),
            reactionCounts = listOf(
                ReactionCount(":watasimo:", 1),
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
        Assert.assertNull(result.myReaction)
        Assert.assertEquals(emptyList<ReactionCount>(), result.reactionCounts)
    }

    @Test
    fun onUnReacted_GiveOtherUserEvent() {
        val note = Note.make(
            id = Note.Id(0L, "1"),
            text = "",
            userId = User.Id(0L, "2"),
            reactionCounts = listOf(
                ReactionCount(":watasimo:", 2),
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
        Assert.assertEquals(":watasimo:", result.myReaction)
        Assert.assertEquals(
            listOf(
                ReactionCount(":watasimo:", 1),
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
                ReactionCount(":watasimo:", 2),
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
                    emoji = Emoji(
                        name = ":kawaii:"
                    )
                )
            )
        )
        Assert.assertEquals(
            listOf(
                Emoji(
                    name = ":kawaii:"
                )
            ), result.emojis
        )
    }

}