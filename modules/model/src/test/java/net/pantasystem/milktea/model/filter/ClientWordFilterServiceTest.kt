package net.pantasystem.milktea.model.filter

import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.make
import net.pantasystem.milktea.model.note.muteword.FilterConditionType
import net.pantasystem.milktea.model.note.muteword.WordFilterConfig
import net.pantasystem.milktea.model.note.poll.Poll
import net.pantasystem.milktea.model.user.User
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ClientWordFilterServiceTest {

    @Test
    fun isShouldFilterNote_GiveMatchedText() {
        val service = ClientWordFilterService()
        val config = WordFilterConfig(
            conditions = listOf(
                FilterConditionType.Normal(
                    words = listOf("test")
                )
            )
        )
        val note = Note.make(
            Note.Id(0L, ""),
            User.Id(0L, ""),
            text = "testtest"
        )
        Assertions.assertTrue(service.isShouldFilterNote(config, note))
    }

    @Test
    fun isShouldFilterNote_GiveMatchedTextInCw() {
        val service = ClientWordFilterService()
        val config = WordFilterConfig(
            conditions = listOf(
                FilterConditionType.Normal(
                    words = listOf("test")
                )
            )
        )
        val note = Note.make(
            Note.Id(0L, ""),
            User.Id(0L, ""),
            text = "hoge",
            cw = "testtest"
        )
        Assertions.assertTrue(service.isShouldFilterNote(config, note))
    }

    @Test
    fun isShouldFilterNote_GiveMatchedTextInPollChoice() {
        val service = ClientWordFilterService()
        val config = WordFilterConfig(
            conditions = listOf(
                FilterConditionType.Normal(
                    words = listOf("test")
                )
            )
        )
        val note = Note.make(
            Note.Id(0L, ""),
            User.Id(0L, ""),
            text = "fuga",
            poll = Poll(
                choices = listOf(
                    "test",
                    "fuga",
                    "piyo"
                ).mapIndexed { index, s ->
                    Poll.Choice(
                        index,
                        s,
                        10,
                        false
                    )
                },
                null,
                false
            )
        )
        Assertions.assertTrue(service.isShouldFilterNote(config, note))
    }

    @Test
    fun isShouldFilterNote_GiveNotMatched() {
        val service = ClientWordFilterService()
        val config = WordFilterConfig(
            conditions = listOf(
                FilterConditionType.Normal(
                    words = listOf("test")
                )
            )
        )
        val note = Note.make(
            Note.Id(0L, ""),
            User.Id(0L, ""),
            text = "f302fjfjoaf",
            poll = Poll(
                choices = listOf(
                    "8f90fj0afa",
                    "390fjvan",
                    "2390fno"
                ).mapIndexed { index, s ->
                    Poll.Choice(
                        index,
                        s,
                        10,
                        false
                    )
                },
                null,
                false
            )
        )
        Assertions.assertFalse(service.isShouldFilterNote(config, note))
    }
}