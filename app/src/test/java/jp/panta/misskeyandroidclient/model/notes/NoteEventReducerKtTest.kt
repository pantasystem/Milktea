package jp.panta.misskeyandroidclient.model.notes

import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionCount
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.streaming.NoteUpdated
import junit.framework.TestCase
import kotlinx.datetime.Clock
import org.junit.Assert
import org.junit.Test

class NoteEventReducerKtTest : TestCase() {


    private val account: Account by lazy {
        Account("remoteId", "test.net", "Panta", "")
    }
    private val note: Note by lazy {
        Note(
            Note.Id(account.accountId, "noteId"),
            Clock.System.now(),
            null, null, User.Id(0L, ""),
            null, null,
            false,
            Visibility.Public(false),
            false,
            null,
            null,
            null,
            0,
            emptyList(),
            emptyList(),
            0,
            null,
            null,
            null,
            null
        )
    }


    @Test
    fun testOnUnReacted() {
        val note = note.copy(
            reactionCounts = listOf(
                ReactionCount("reacted", 1)
            )
        )
        val updatedNote = note.unReacted(account, reaction = "reacted", userId = account.remoteId)

        Assert.assertNull(updatedNote.myReaction)
        Assert.assertEquals(true, updatedNote.reactionCounts.isEmpty())
    }

    @Test
    fun testOnUnReactedHasMultipleReaction() {
        val note = note.copy(
            reactionCounts = listOf(
                ReactionCount("reacted", 2),
                ReactionCount("emoji", 3)
            ),
        )
        val updated = note.unReacted(account, reaction = "reacted", userId = account.remoteId)
        Assert.assertEquals(listOf(
            ReactionCount("reacted", 1),
            ReactionCount("emoji", 3)
        ), updated.reactionCounts)
        Assert.assertNull(updated.myReaction)
    }

    @Test
    fun testOnUnReactedHasMultipleReactionAndMyReaction() {
        val note = note.copy(
            reactionCounts = listOf(
                ReactionCount("reacted", 2),
                ReactionCount("emoji", 3)
            ),
            myReaction = "reacted"
        )
        val updated = note.unReacted(account, reaction = "reacted", userId = account.remoteId)
        Assert.assertEquals(listOf(
            ReactionCount("reacted", 1),
            ReactionCount("emoji", 3)
        ), updated.reactionCounts)
        Assert.assertNull(updated.myReaction)
    }

    @Test
    fun testReactedWhenOtherUserReaction() {
        val updatedNote = note.reacted(account, reaction = "reacted", userId = "otherUserId", null)
        Assert.assertNull( updatedNote.myReaction)
        Assert.assertEquals("reacted", updatedNote.reactionCounts.first().reaction)
        Assert.assertEquals(1, updatedNote.reactionCounts.first().count)
    }

    @Test
    fun testOnReacted() {

        val updatedNote = note.reacted(account, reaction = "reacted", userId = account.remoteId, null)
        Assert.assertEquals("reacted", updatedNote.myReaction)
        Assert.assertEquals("reacted", updatedNote.reactionCounts.first().reaction)
        Assert.assertEquals(1, updatedNote.reactionCounts.first().count)

    }



    @Test
    fun testOnIReacted() {
        val updatedNote = note.onIReacted("reacted")
        Assert.assertEquals("reacted", updatedNote.myReaction)
        Assert.assertEquals("reacted", updatedNote.reactionCounts.first().reaction)
        Assert.assertEquals(1, updatedNote.reactionCounts.first().count)
    }

    @Test
    fun testOnIUnReacted() {
        val note = note.copy(
            reactionCounts = listOf(
                ReactionCount("reacted", 1),
            ),
            myReaction = "reacted"
        )
        val updatedNote = note.onIUnReacted()
        Assert.assertNull(updatedNote.myReaction)
        Assert.assertEquals(true, updatedNote.reactionCounts.isEmpty())
    }
}