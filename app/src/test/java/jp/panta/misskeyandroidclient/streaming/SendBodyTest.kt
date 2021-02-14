package jp.panta.misskeyandroidclient.streaming

import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Test

class SendBodyTest {

    @Test
    fun testParseMain() {
        val main = Send(SendBody.Connect.Main())

        val h = Json.encodeToString(main)
        println(h)
        Assert.assertTrue(true)
    }
}