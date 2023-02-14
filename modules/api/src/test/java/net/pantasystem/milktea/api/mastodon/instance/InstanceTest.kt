package net.pantasystem.milktea.api.mastodon.instance

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.pantasystem.milktea.api.CurrentClassLoader
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class InstanceTest {

    @Test
    fun decodeFromStringGiveFedibird() {
        val json = Json {
            ignoreUnknownKeys = true
        }
        val file = File(CurrentClassLoader()!!.getResource("fedibird_instance_info.json").file)
        val text = BufferedReader(InputStreamReader(BufferedInputStream(file.inputStream()))).use {
            it.readLines().reduce { acc, s -> acc + s }.trimIndent()
        }

        json.decodeFromString<Instance>(text)
    }

    @Test
    fun decodeFromStringGiveMstdnJp() {
        val json = Json {
            ignoreUnknownKeys = true
        }
        val file = File(CurrentClassLoader()!!.getResource("mstdnjp_instance_info.json").file)
        val text = BufferedReader(InputStreamReader(BufferedInputStream(file.inputStream()))).use {
            it.readLines().reduce { acc, s -> acc + s }.trimIndent()
        }

        json.decodeFromString<Instance>(text)
    }

    @Test
    fun decodeFromStringGiveMastodonSocial() {
        val json = Json {
            ignoreUnknownKeys = true
        }
        val file = File(CurrentClassLoader()!!.getResource("mastodonsocial_instance_info.json").file)
        val text = BufferedReader(InputStreamReader(BufferedInputStream(file.inputStream()))).use {
            it.readLines().reduce { acc, s -> acc + s }.trimIndent()
        }

        val instance = json.decodeFromString<Instance>(text)
        Assertions.assertEquals(4, instance.configuration?.polls?.maxOptions)
        Assertions.assertEquals(50, instance.configuration?.polls?.maxCharactersPerOption)
        Assertions.assertEquals(300, instance.configuration?.polls?.minExpiration)
        Assertions.assertEquals(2629746, instance.configuration?.polls?.maxExpiration)

        Assertions.assertNotNull(instance.configuration)
        Assertions.assertEquals(500, instance.configuration?.statuses?.maxCharacters)
        Assertions.assertEquals(4, instance.configuration?.statuses?.maxMediaAttachments)
    }


    @Test
    fun decodeFromStringGivePawooNet() {
        val json = Json {
            ignoreUnknownKeys = true
        }
        val file = File(CurrentClassLoader()!!.getResource("pawoonet_instance_info.json").file)
        val text = BufferedReader(InputStreamReader(BufferedInputStream(file.inputStream()))).use {
            it.readLines().reduce { acc, s -> acc + s }.trimIndent()
        }

        json.decodeFromString<Instance>(text)
    }


}