package jp.panta.misskeyandroidclient.model.notes.poll

import kotlinx.datetime.Clock
import net.pantasystem.milktea.model.notes.poll.Poll
import org.junit.Assert
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

class PollTest {


    @Test
    fun totalVoteCount() {
        val poll = net.pantasystem.milktea.model.notes.poll.Poll(
            choices = listOf(
                net.pantasystem.milktea.model.notes.poll.Poll.Choice(
                    index = 0,
                    votes = 10,
                    text = "aaa",
                    isVoted = false
                ),
                net.pantasystem.milktea.model.notes.poll.Poll.Choice(
                    index = 0,
                    votes = 20,
                    text = "aaa",
                    isVoted = false
                ),
                net.pantasystem.milktea.model.notes.poll.Poll.Choice(
                    index = 0,
                    votes = 15,
                    text = "aaa",
                    isVoted = false
                ),
                net.pantasystem.milktea.model.notes.poll.Poll.Choice(
                    index = 0,
                    votes = 8,
                    text = "aaa",
                    isVoted = false
                ),
            ),
            expiresAt = null,
            multiple = false
        )
        Assert.assertEquals(53, poll.totalVoteCount)
    }

    @Test
    fun canVoteMultiplePoll() {
        val poll = net.pantasystem.milktea.model.notes.poll.Poll(
            choices = listOf(
                net.pantasystem.milktea.model.notes.poll.Poll.Choice(
                    index = 0,
                    votes = 10,
                    text = "aaa",
                    isVoted = true
                ),
                net.pantasystem.milktea.model.notes.poll.Poll.Choice(
                    index = 0,
                    votes = 20,
                    text = "aaa",
                    isVoted = true
                ),
                net.pantasystem.milktea.model.notes.poll.Poll.Choice(
                    index = 0,
                    votes = 15,
                    text = "aaa",
                    isVoted = false
                ),
                net.pantasystem.milktea.model.notes.poll.Poll.Choice(
                    index = 0,
                    votes = 8,
                    text = "aaa",
                    isVoted = false
                ),
            ),
            expiresAt = null,
            multiple = true
        )
        Assert.assertTrue(poll.canVote)
    }

    @Test
    fun canVote() {
        val poll = net.pantasystem.milktea.model.notes.poll.Poll(
            choices = listOf(
                net.pantasystem.milktea.model.notes.poll.Poll.Choice(
                    index = 0,
                    votes = 10,
                    text = "aaa",
                    isVoted = false
                ),
                net.pantasystem.milktea.model.notes.poll.Poll.Choice(
                    index = 0,
                    votes = 20,
                    text = "aaa",
                    isVoted = false
                ),
                net.pantasystem.milktea.model.notes.poll.Poll.Choice(
                    index = 0,
                    votes = 15,
                    text = "aaa",
                    isVoted = false
                ),
                net.pantasystem.milktea.model.notes.poll.Poll.Choice(
                    index = 0,
                    votes = 8,
                    text = "aaa",
                    isVoted = false
                ),
            ),
            expiresAt = null,
            multiple = false
        )
        Assert.assertTrue(poll.canVote)
    }

    @Test
    fun canVoteWhenVoted() {
        val poll = net.pantasystem.milktea.model.notes.poll.Poll(
            choices = listOf(
                net.pantasystem.milktea.model.notes.poll.Poll.Choice(
                    index = 0,
                    votes = 10,
                    text = "aaa",
                    isVoted = true
                ),
                net.pantasystem.milktea.model.notes.poll.Poll.Choice(
                    index = 0,
                    votes = 20,
                    text = "aaa",
                    isVoted = false
                ),
                net.pantasystem.milktea.model.notes.poll.Poll.Choice(
                    index = 0,
                    votes = 15,
                    text = "aaa",
                    isVoted = false
                ),
                net.pantasystem.milktea.model.notes.poll.Poll.Choice(
                    index = 0,
                    votes = 8,
                    text = "aaa",
                    isVoted = false
                ),
            ),
            expiresAt = null,
            multiple = false
        )
        Assert.assertFalse(poll.canVote)
    }

    @Test
    fun canVoteWithinExpiredAt() {
        val poll = net.pantasystem.milktea.model.notes.poll.Poll(
            choices = listOf(
                net.pantasystem.milktea.model.notes.poll.Poll.Choice(
                    index = 0,
                    votes = 10,
                    text = "aaa",
                    isVoted = false
                ),
                net.pantasystem.milktea.model.notes.poll.Poll.Choice(
                    index = 0,
                    votes = 20,
                    text = "aaa",
                    isVoted = false
                ),
                net.pantasystem.milktea.model.notes.poll.Poll.Choice(
                    index = 0,
                    votes = 15,
                    text = "aaa",
                    isVoted = false
                ),
                net.pantasystem.milktea.model.notes.poll.Poll.Choice(
                    index = 0,
                    votes = 8,
                    text = "aaa",
                    isVoted = false
                ),
            ),
            expiresAt = Clock.System.now() + 10.minutes,
            multiple = false
        )
        Assert.assertTrue(poll.canVote)
    }
    @Test
    fun canVoteWhenExpired() {
        val poll = net.pantasystem.milktea.model.notes.poll.Poll(
            choices = listOf(
                net.pantasystem.milktea.model.notes.poll.Poll.Choice(
                    index = 0,
                    votes = 10,
                    text = "aaa",
                    isVoted = false
                ),
                net.pantasystem.milktea.model.notes.poll.Poll.Choice(
                    index = 0,
                    votes = 20,
                    text = "aaa",
                    isVoted = false
                ),
                net.pantasystem.milktea.model.notes.poll.Poll.Choice(
                    index = 0,
                    votes = 15,
                    text = "aaa",
                    isVoted = false
                ),
                net.pantasystem.milktea.model.notes.poll.Poll.Choice(
                    index = 0,
                    votes = 8,
                    text = "aaa",
                    isVoted = false
                ),
            ),
            expiresAt = Clock.System.now() - 1.minutes,
            multiple = false
        )
        Assert.assertFalse(poll.canVote)
    }

    @Test
    fun canVoteVotedAndExpired() {
        val poll = net.pantasystem.milktea.model.notes.poll.Poll(
            choices = listOf(
                net.pantasystem.milktea.model.notes.poll.Poll.Choice(
                    index = 0,
                    votes = 10,
                    text = "aaa",
                    isVoted = false
                ),
                net.pantasystem.milktea.model.notes.poll.Poll.Choice(
                    index = 0,
                    votes = 20,
                    text = "aaa",
                    isVoted = true
                ),
            ),
            expiresAt = Clock.System.now() - 1.minutes,
            multiple = false
        )
        Assert.assertFalse(poll.canVote)
    }

    @Test
    fun canVoteAllVotedAndMultiple() {
        val poll = net.pantasystem.milktea.model.notes.poll.Poll(
            choices = listOf(
                net.pantasystem.milktea.model.notes.poll.Poll.Choice(
                    index = 0,
                    votes = 10,
                    text = "aaa",
                    isVoted = true
                ),
                net.pantasystem.milktea.model.notes.poll.Poll.Choice(
                    index = 0,
                    votes = 20,
                    text = "aaa",
                    isVoted = true
                ),
            ),
            multiple = true,
            expiresAt = null
        )
        Assert.assertFalse(poll.canVote)
    }
}