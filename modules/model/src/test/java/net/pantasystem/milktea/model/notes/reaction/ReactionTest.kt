package net.pantasystem.milktea.model.notes.reaction

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ReactionTest {

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
}