package net.pantasystem.milktea.api.misskey.infos

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

class InstanceInfosResponseTest {

    @Test
    fun decode() {
        val json = Json {
            ignoreUnknownKeys = true
        }

        val url = URL("https://instanceapp.misskey.page/instances.json")

        val text = BufferedReader(InputStreamReader(BufferedInputStream(url.openStream()))).use {
            it.readLines().reduce { acc, s -> acc + s }.trimIndent()
        }
        json.decodeFromString<InstanceInfosResponse>(text)
    }
}