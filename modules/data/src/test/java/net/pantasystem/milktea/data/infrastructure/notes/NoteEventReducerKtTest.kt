package net.pantasystem.milktea.data.infrastructure.notes

import net.pantasystem.milktea.api_streaming.NoteUpdated
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.make
import net.pantasystem.milktea.model.notes.reaction.ReactionCount
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
            )
        )

        Assertions.assertEquals(listOf(ReactionCount(":kawaii:", 1)), result.reactionCounts)
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
            )
        )

        Assertions.assertEquals(listOf(ReactionCount(":kawaii:", 1)), result.reactionCounts)
        Assertions.assertEquals(":kawaii:", result.myReaction)
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

        Assertions.assertEquals(
            listOf(ReactionCount(":watasimo:", 1), ReactionCount(":kawaii:", 2)),
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

        Assertions.assertEquals(
            listOf(ReactionCount(":watasimo:", 1), ReactionCount(":kawaii:", 1)),
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
        Assertions.assertEquals(":watasimo:", result.myReaction)
        Assertions.assertEquals(
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
        Assertions.assertEquals(
            listOf(
                Emoji(
                    name = ":kawaii:"
                )
            ), result.emojis
        )
    }

}