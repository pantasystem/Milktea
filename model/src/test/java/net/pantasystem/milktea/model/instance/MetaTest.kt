package net.pantasystem.milktea.model.instance

import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.notes.reaction.Reaction
import org.junit.Assert.*

import org.junit.Test

class MetaTest {

    @Test
    fun getVersion() {

        val meta = Meta(
            uri = "https://misskey.io",
            version = "12.7.54"
        )
        val version = meta.getVersion()
        assertEquals(Version("12.7.54"), version)
    }

    @Test
    fun isOwnEmojiBy() {
        val emojiSources = listOf("a", "b", "c", "d", "e", "f", "g")
        val meta = Meta(
            uri = "https://misskey.io",
            emojis = emojiSources.map {
                Emoji(name = it)
            }
        )
        assertTrue(meta.isOwnEmojiBy(Reaction("a")))
        assertTrue(meta.isOwnEmojiBy(Reaction(":b:")))
    }
}