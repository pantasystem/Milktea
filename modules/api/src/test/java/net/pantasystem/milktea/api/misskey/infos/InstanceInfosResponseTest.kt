package net.pantasystem.milktea.api.misskey.infos

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class InstanceInfosResponseTest {

    @Test
    fun decode() {
        val json = Json {
            ignoreUnknownKeys = true
        }
        val file = File(javaClass.classLoader!!.getResource("instances_info.json").file)
        val text = BufferedReader(InputStreamReader(BufferedInputStream(file.inputStream()))).use {
            it.readLines().reduce { acc, s -> acc + s }.trimIndent()
        }
        json.decodeFromString<InstanceInfosResponse>(text)
    }
}