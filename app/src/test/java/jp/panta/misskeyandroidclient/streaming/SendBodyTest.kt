package jp.panta.misskeyandroidclient.streaming

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Test

class SendBodyTest {

    @Test
    fun testParseMain() {
        val main = Send(Body.Connect(Body.Connect.Body(channel = Body.Connect.Type.HOME_TIMELINE, id = "hoge")));

        val h = Json.encodeToString(main)
        println(h)
        //println(Gson().toJson(Send(SendBody.Connect.HomeTimeline())))
        Assert.assertTrue(true)
    }
}