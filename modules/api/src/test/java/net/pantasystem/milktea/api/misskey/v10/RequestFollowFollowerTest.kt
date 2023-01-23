package net.pantasystem.milktea.api.misskey.v10

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class RequestFollowFollowerTest {

    private val encoder by lazy {
        Json {
            ignoreUnknownKeys = true
        }
    }
    @Test
    fun encodeJsonTest() {
        val req = RequestFollowFollower(
            i = "test",
            cursor = null,
            userId = "5acddf5fcd7aebd97984769a"
        )
        val strJson = encoder.encodeToString(req)
        println(strJson)
        Assertions.assertEquals("""{"i":"test","userId":"5acddf5fcd7aebd97984769a"}""", strJson)

    }

    @Test
    fun encodeJsonTestGiveCursor() {
        val req = RequestFollowFollower(
            i = "test",
            cursor = "nextId",
            userId = "5acddf5fcd7aebd97984769a"
        )
        val strJson = encoder.encodeToString(req)
        println(strJson)
        Assertions.assertEquals(
            """{"i":"test","userId":"5acddf5fcd7aebd97984769a","cursor":"nextId"}""",
            strJson
        )

    }
}