package jp.panta.misskeyandroidclient.model.notes.reaction

import org.junit.Assert.*

import org.junit.Test

class ReactionTest {

    @Test
    fun isLocal() {
        val localReaction = Reaction(":kawaii@.:")
        assertTrue(localReaction.isLocal())
    }

    @Test
    fun isNotLocal() {
        val localReaction = Reaction(":kawaii@misskey.io:")
        assertFalse(localReaction.isLocal())
    }

    @Test
    fun getName() {
        val reaction = Reaction(":a:")
        assertEquals("a", reaction.getName())
    }

    @Test
    fun getNameThenRemoteCustomEmojiReaction() {
        val reaction = Reaction(":a@misskey.io:")
        assertEquals("a", reaction.getName())
    }

    @Test
    fun getNameThenLocalCustomEmojiReaction() {
        val reaction = Reaction(":a@.:")
        assertEquals("a", reaction.getName())
    }

    @Test
    fun isCustomEmojiFormatThenCustomEmoji() {
        val reaction = Reaction(":a:")
        assertTrue(reaction.isCustomEmojiFormat())
    }

    @Test
    fun isCustomEmojiFormatThenRemoteCustomEmoji() {
        val reaction = Reaction(":a@misskey.io:")
        assertTrue(reaction.isCustomEmojiFormat())
    }

    @Test
    fun isCustomEmojiFormatThenLocalCustomEmoji() {
        val reaction = Reaction(":a@.:")
        assertTrue(reaction.isCustomEmojiFormat())
    }

    @Test
    fun isCustomEmojiFormatThenEmoji() {
        val reaction = Reaction("😄")
        assertFalse(reaction.isCustomEmojiFormat())
    }
}