package net.pantasystem.milktea.api.misskey.notes

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


class CreateNoteTest {

    @Test
    fun parseTest() {
        val json = Json {
            ignoreUnknownKeys = true
        }
        val createNote = CreateNote(
            i = "test",
            text = "hogehoge",
            visibility = "specified",
            renoteId = "renote-id"
        )
        val str = json.encodeToString(createNote)
        Assertions.assertEquals(
            """{"i":"test","visibility":"specified","text":"hogehoge","renoteId":"renote-id"}""",
            str
        )
    }
}