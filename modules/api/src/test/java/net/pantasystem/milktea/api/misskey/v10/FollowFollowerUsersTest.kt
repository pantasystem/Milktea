package net.pantasystem.milktea.api.misskey.v10

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class FollowFollowerUsersTest {

    @Test
    fun decodeJsonTest() {
        val file = File(javaClass.classLoader!!.getResource("v10_followers_case1.json").file)
        BufferedReader(InputStreamReader(BufferedInputStream(file.inputStream()))).use { reader ->
            val textJson = reader.readLines().reduce { acc, s -> acc + s }.trimIndent()
            val decoder = Json {
                ignoreUnknownKeys = true
            }
            val dto = decoder.decodeFromString<FollowFollowerUsers>(textJson)
            Assertions.assertEquals(100, dto.users.size)
            println(dto)
        }
    }
}