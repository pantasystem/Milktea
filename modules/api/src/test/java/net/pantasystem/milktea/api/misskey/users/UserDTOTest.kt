package net.pantasystem.milktea.api.misskey.users

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.pantasystem.milktea.api.CurrentClassLoader
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class UserDTOTest {

    @Test
    fun convertFromJson() {
        val json = Json {
            ignoreUnknownKeys = true
        }
        val file = File(CurrentClassLoader()!!.getResource("user_dto_list_case1.json").file)
        val testData = BufferedReader(InputStreamReader(BufferedInputStream(file.inputStream()))).use {
            it.readLines().reduce { acc, s -> acc + s }.trimIndent()
        }
        val userDTOList = json.decodeFromString<List<UserDTO>>(testData)
        Assertions.assertNotNull(userDTOList.getOrNull(0))
        Assertions.assertEquals("harunon", userDTOList[0].userName)

    }

    @Test
    fun convertFromJson_GiveHarunon() {
        val json = Json {
            ignoreUnknownKeys = true
        }
        val file = File(CurrentClassLoader()!!.getResource("user_dto_give_harunon_case1.json").file)
        val testData = BufferedReader(InputStreamReader(BufferedInputStream(file.inputStream()))).use {
            it.readLines().reduce { acc, s -> acc + s }.trimIndent()
        }

        val harunon = json.decodeFromString<UserDTO>(testData)
        Assertions.assertEquals("keybase", harunon.fields?.get(0)?.name)
        Assertions.assertEquals("https://keybase.io/harunon", harunon.fields?.get(0)?.value)
        Assertions.assertEquals("ぢすこ", harunon.fields?.get(1)?.name)

    }

    @Test
    fun convertFromJson_GiveV10Harunon() {
        val json = Json {
            ignoreUnknownKeys = true
        }
        val file = File(CurrentClassLoader()!!.getResource("v10_user_dto_give_harunon_case1.json").file)
        val testData = BufferedReader(InputStreamReader(BufferedInputStream(file.inputStream()))).use {
            it.readLines().reduce { acc, s -> acc + s }.trimIndent()
        }
        val harunon = json.decodeFromString<UserDTO>(testData)
        Assertions.assertEquals("keybase", harunon.fields?.get(0)?.name)
        Assertions.assertEquals("https://keybase.io/harunon", harunon.fields?.get(0)?.value)
        Assertions.assertEquals("ぢすこ", harunon.fields?.get(1)?.name)

    }
}