package jp.panta.misskeyandroidclient.streaming

import com.google.gson.Gson
import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Test

class SendBodyTest {

    @Test
    fun testParseMain() {
        val main = Send(SendBody.Connect.HomeTimeline(channel = "homeTimeline"))

        val h = Json.encodeToString(main)
        println(h)
        //println(Gson().toJson(Send(SendBody.Connect.HomeTimeline())))
        Assert.assertTrue(true)
    }
}