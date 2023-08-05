package net.pantasystem.milktea.model.instance

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class MetaTest {

    @Test
    fun getVersion() {

        val meta = Meta(
            uri = "https://misskey.io",
            version = "12.7.54"
        )
        val version = meta.getVersion()
        assertEquals(Version("12.7.54"), version)
    }


    @Test
    fun decodeJsonGiveV12Meta() {
        val file = File(javaClass.classLoader!!.getResource("v12_meta_case1.json").file)
        val reader = BufferedReader(InputStreamReader(BufferedInputStream(file.inputStream())))
        val textJson = reader.readLines().reduce { acc, s -> acc + s }.trimIndent()
        val decoder = Json {
            ignoreUnknownKeys = true
        }
        val meta= decoder.decodeFromString<Meta>(textJson)
        assertEquals("Misskey.io", meta.name)
        assertEquals("12.110.1", meta.version)
    }

    @Test
    fun decodeJsonGiveV10Meta() {
        val file = File(javaClass.classLoader!!.getResource("v10_meta_case1.json").file)
        val reader = BufferedReader(InputStreamReader(BufferedInputStream(file.inputStream())))
        val textJson = reader.readLines().reduce { acc, s -> acc + s }.trimIndent()
        val decoder = Json {
            ignoreUnknownKeys = true
        }
        val meta = decoder.decodeFromString<Meta>(textJson)
        assertEquals("めいすきー", meta.name)
        assertEquals("10.102.584-m544", meta.version)

    }
}