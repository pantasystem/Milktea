package jp.panta.misskeyandroidclient.api.misskey.v12.channel

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.pantasystem.milktea.api.misskey.v12.channel.ChannelDTO
import net.pantasystem.milktea.model.account.Account

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


class ChannelDTOTest {

    @Test
    fun toModel() {
        val jsonStr = """{
            "id": "8rfhsu910l",
            "createdAt": "2021-10-04T00:42:07.525Z",
            "lastNotedAt": "2022-03-20T18:41:43.817Z",
            "name": "はるのんしすてむどっとこむ",
            "description": "消えたので作った",
            "userId": "7rla9gie6j",
            "bannerUrl": null,
            "usersCount": 13,
            "notesCount": 375,
            "isFollowing": true,
            "hasUnreadNote": true
        }"""
        val parser = Json {
            ignoreUnknownKeys = true
        }
        val channelDTO: ChannelDTO = parser.decodeFromString(jsonStr)
        assertEquals(true, channelDTO.isFollowing)
        assertEquals(true, channelDTO.hasUnreadNote)
        assertEquals(13, channelDTO.usersCount)
        assertNull(channelDTO.bannerUrl)
        assertEquals("8rfhsu910l", channelDTO.id)

        val channel = channelDTO.toModel(
            Account(
                "id",
                "misskey.io",
                "Panta",
                Account.InstanceType.MISSKEY,
                ""
            )
        )
        assertEquals(channelDTO.id, channel.id.channelId)
        assertEquals(channelDTO.isFollowing, channel.isFollowing)
        assertEquals(channelDTO.hasUnreadNote, channel.hasUnreadNote)
        assertEquals(channelDTO.name, channel.name)

    }

    @Test
    fun decodeJsonUnAuthData() {
        val jsonStr = """{
            "id": "8rfhsu910l",
            "createdAt": "2021-10-04T00:42:07.525Z",
            "lastNotedAt": "2022-03-20T18:41:43.817Z",
            "name": "はるのんしすてむどっとこむ",
            "description": "消えたので作った",
            "userId": "7rla9gie6j",
            "bannerUrl": null,
            "usersCount": 13,
            "notesCount": 375
            }"""

        val parser = Json {
            ignoreUnknownKeys = true
        }
        val channelDTO: ChannelDTO = parser.decodeFromString(jsonStr)
        assertNull(channelDTO.isFollowing)
        assertNull(channelDTO.hasUnreadNote)
    }

    @Test
    fun decodeJsonAuthorizedData() {
        val jsonStr = """{
            "id": "8rfhsu910l",
            "createdAt": "2021-10-04T00:42:07.525Z",
            "lastNotedAt": "2022-03-20T18:41:43.817Z",
            "name": "はるのんしすてむどっとこむ",
            "description": "消えたので作った",
            "userId": "7rla9gie6j",
            "bannerUrl": null,
            "usersCount": 13,
            "notesCount": 375,
            "isFollowing": true,
            "hasUnreadNote": true
        }"""
        val parser = Json {
            ignoreUnknownKeys = true
        }
        val channelDTO: ChannelDTO = parser.decodeFromString(jsonStr)
        assertEquals(true, channelDTO.isFollowing)
        assertEquals(true, channelDTO.hasUnreadNote)
        assertEquals(13, channelDTO.usersCount)
        assertNull(channelDTO.bannerUrl)
        assertEquals("8rfhsu910l", channelDTO.id)
        assertEquals("はるのんしすてむどっとこむ", channelDTO.name)
        assertEquals("消えたので作った", channelDTO.description)
        assertEquals(375, channelDTO.notesCount)
    }

    @Test
    fun decodeJsonDescriptionIsNull() {
        val jsonStr = """{
            "id": "8rfhsu910l",
            "createdAt": "2021-10-04T00:42:07.525Z",
            "lastNotedAt": "2022-03-20T18:41:43.817Z",
            "name": "はるのんしすてむどっとこむ",
            "description": null,
            "userId": "7rla9gie6j",
            "bannerUrl": null,
            "usersCount": 13,
            "notesCount": 375,
            "isFollowing": true,
            "hasUnreadNote": true
        }"""
        val parser = Json {
            ignoreUnknownKeys = true
        }
        val channelDTO: ChannelDTO = parser.decodeFromString(jsonStr)
        assertNull(channelDTO.description)
    }
}