package net.pantasystem.milktea.model.note.reaction

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ReactionTest {

    @Test
    fun isLocal() {
        val localReaction = Reaction(":kawaii@.:")
        Assertions.assertTrue(localReaction.isLocal())
    }

    @Test
    fun isNotLocal() {
        val localReaction = Reaction(":kawaii@misskey.io:")
        Assertions.assertFalse(localReaction.isLocal())
    }

    @Test
    fun getName() {
        val reaction = Reaction(":a:")
        Assertions.assertEquals("a", reaction.getName())
    }

    @Test
    fun getNameThenRemoteCustomEmojiReaction() {
        val reaction = Reaction(":a@misskey.io:")
        Assertions.assertEquals("a", reaction.getName())
    }

    @Test
    fun getNameThenLocalCustomEmojiReaction() {
        val reaction = Reaction(":a@.:")
        Assertions.assertEquals("a", reaction.getName())
    }

    @Test
    fun isCustomEmojiFormatThenCustomEmoji() {
        val reaction = Reaction(":a:")
        Assertions.assertTrue(reaction.isCustomEmojiFormat())
    }

    @Test
    fun isCustomEmojiFormatThenRemoteCustomEmoji() {
        val reaction = Reaction(":a@misskey.io:")
        Assertions.assertTrue(reaction.isCustomEmojiFormat())
    }

    @Test
    fun isCustomEmojiFormatThenLocalCustomEmoji() {
        val reaction = Reaction(":a@.:")
        Assertions.assertTrue(reaction.isCustomEmojiFormat())
    }

    @Test
    fun isCustomEmojiFormatThenEmoji() {
        val reaction = Reaction("😄")
        Assertions.assertFalse(reaction.isCustomEmojiFormat())
    }

    @Test
    fun getName_GiveColonNameColon() {
        val reaction = Reaction(":name:")
        Assertions.assertEquals("name", reaction.getName())
    }

    @Test
    fun getHost_GiveColonNameColon() {
        val reaction = Reaction(":name:")
        Assertions.assertNull(reaction.getHost())
    }

    @Test
    fun getHost_GiveColonNameAtMarkHostColon() {
        val reaction = Reaction(":name@host:")
        Assertions.assertEquals("host",reaction.getHost())
    }

    @Test
    fun getNameAndHost_GiveColonNameColon() {
        val reaction = Reaction(":name:")
        Assertions.assertEquals("name", reaction.getNameAndHost())
    }

    @Test
    fun getNameAndHost_GiveColonNameAtMarkHostColon() {
        val reaction = Reaction(":name@host:")
        Assertions.assertEquals("name@host", reaction.getNameAndHost())
    }

    @Test
    fun getHost_GiveNameAtMarkDot() {
        val reaction = Reaction("name@.")
        Assertions.assertNull(reaction.getHost())
    }

    @Test
    fun getNameAndHost_GiveNameAtMarkDot() {
        val reaction = Reaction("name@.")
        Assertions.assertEquals("name", reaction.getNameAndHost())
    }

    @Test
    fun isCustomEmojiFormat_GiveLegacyEmoji() {
        LegacyReaction.defaultReaction.forEach {
            Assertions.assertFalse(Reaction(it).isCustomEmojiFormat())
            Assertions.assertTrue(Reaction(it).isLegacyFormat())
            Assertions.assertEquals(LegacyReaction.reactionMap[it], Reaction(it).getLegacyEmoji())
        }
    }
}