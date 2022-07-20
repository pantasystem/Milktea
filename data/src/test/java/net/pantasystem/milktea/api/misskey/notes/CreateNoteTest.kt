package net.pantasystem.milktea.api.misskey.notes

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Test

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
        Assert.assertEquals(
            """{"i":"test","visibility":"specified","text":"hogehoge","renoteId":"renote-id"}""",
            str
        )
    }
}