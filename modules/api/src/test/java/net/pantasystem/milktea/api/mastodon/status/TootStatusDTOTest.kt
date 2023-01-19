package net.pantasystem.milktea.api.mastodon.status

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class TootStatusDTOTest {

    @Test
    fun decodeTest() {
        val json = Json {
            ignoreUnknownKeys = true
        }
        val file = File(javaClass.classLoader!!.getResource("toot_fedibird_com_home_timeline.json").file)
        val text = BufferedReader(InputStreamReader(BufferedInputStream(file.inputStream()))).use {
            it.readLines().reduce { acc, s -> acc + s }.trimIndent()
        }

        json.decodeFromString<List<TootStatusDTO>>(text)

    }


    @Test
    fun decodeTest2() {
        val json = Json {
            ignoreUnknownKeys = true
        }
        val file = File(javaClass.classLoader!!.getResource("toot_fedibird_com_home_timeline_2.json").file)
        val text = BufferedReader(InputStreamReader(BufferedInputStream(file.inputStream()))).use {
            it.readLines().reduce { acc, s -> acc + s }.trimIndent()
        }

        json.decodeFromString<List<TootStatusDTO>>(text)

    }


    @Test
    fun decodeTest3() {
        val json = Json {
            ignoreUnknownKeys = true
        }
        val file = File(javaClass.classLoader!!.getResource("toot_fedibird_com_home_timeline_3.json").file)
        val text = BufferedReader(InputStreamReader(BufferedInputStream(file.inputStream()))).use {
            it.readLines().reduce { acc, s -> acc + s }.trimIndent()
        }

        json.decodeFromString<List<TootStatusDTO>>(text)

    }


    @Test
    fun decodeTest4() {
        val json = Json {
            ignoreUnknownKeys = true
        }
        val file = File(javaClass.classLoader!!.getResource("toot_fedibird_com_home_timeline_4.json").file)
        val text = BufferedReader(InputStreamReader(BufferedInputStream(file.inputStream()))).use {
            it.readLines().reduce { acc, s -> acc + s }.trimIndent()
        }

        json.decodeFromString<List<TootStatusDTO>>(text)


    }


    @Test
    fun decodeTest5() {
        val json = Json {
            ignoreUnknownKeys = true
        }
        val file = File(javaClass.classLoader!!.getResource("toot_fedibird_com_home_timeline_5.json").file)
        val text = BufferedReader(InputStreamReader(BufferedInputStream(file.inputStream()))).use {
            it.readLines().reduce { acc, s -> acc + s }.trimIndent()
        }

        json.decodeFromString<List<TootStatusDTO>>(text)


    }


    @Test
    fun decodeTest6() {
        val json = Json {
            ignoreUnknownKeys = true
        }
        val file = File(javaClass.classLoader!!.getResource("toot_fedibird_com_home_timeline_6.json").file)
        val text = BufferedReader(InputStreamReader(BufferedInputStream(file.inputStream()))).use {
            it.readLines().reduce { acc, s -> acc + s }.trimIndent()
        }

        json.decodeFromString<List<TootStatusDTO>>(text)

    }



    @Test
    fun decodeTest7() {
        val json = Json {
            ignoreUnknownKeys = true
        }
        val file = File(javaClass.classLoader!!.getResource("toot_fedibird_com_home_timeline_7.json").file)
        val text = BufferedReader(InputStreamReader(BufferedInputStream(file.inputStream()))).use {
            it.readLines().reduce { acc, s -> acc + s }.trimIndent()
        }

        json.decodeFromString<List<TootStatusDTO>>(text)

    }


    @Test
    fun decodeTest8() {
        val json = Json {
            ignoreUnknownKeys = true
        }
        val file = File(javaClass.classLoader!!.getResource("toot_fedibird_com_home_timeline_8.json").file)
        val text = BufferedReader(InputStreamReader(BufferedInputStream(file.inputStream()))).use {
            it.readLines().reduce { acc, s -> acc + s }.trimIndent()
        }

        json.decodeFromString<List<TootStatusDTO>>(text)
    }

    @Test
    fun decodeMstdnJpTimeline() {
        val json = Json {
            ignoreUnknownKeys = true
        }
        val file = File(javaClass.classLoader!!.getResource("toot_mstdn_jp_public_timeline.json").file)
        val text = BufferedReader(InputStreamReader(BufferedInputStream(file.inputStream()))).use {
            it.readLines().reduce { acc, s -> acc + s }.trimIndent()
        }

        json.decodeFromString<List<TootStatusDTO>>(text)
    }

}