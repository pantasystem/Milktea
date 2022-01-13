package jp.panta.misskeyandroidclient.model.notes.reaction

import junit.framework.TestCase
import org.junit.Assert

class ReactionCountTest : TestCase() {

    fun testIsLocal() {
        val reactionCount = ReactionCount(
            "reacted@.",
            1
        )
        Assert.assertTrue(reactionCount.isLocal())
    }

    fun testIsLocalWhenNotLocalReaction() {
        val reactionCount = ReactionCount(
            "reacted@hoge.com",
            1
        )
        Assert.assertFalse(reactionCount.isLocal())
    }

    fun testIncrement() {
        val reactionCount = ReactionCount(
            "reaction@.",
            1
        )
        val incremented = reactionCount.increment()
        Assert.assertEquals(2, incremented.count)
    }

    fun testDecrement() {
        val reactionCount = ReactionCount(
            "reaction@.",
            1
        )
        val decremented = reactionCount.decrement()
        Assert.assertEquals(0, decremented.count)
    }

    fun testDecrementWhenCount0() {
        val reactionCount = ReactionCount(
            "reaction@.",
            0
        )
        val decremented = reactionCount.decrement()
        Assert.assertEquals(0, decremented.count)
    }
}