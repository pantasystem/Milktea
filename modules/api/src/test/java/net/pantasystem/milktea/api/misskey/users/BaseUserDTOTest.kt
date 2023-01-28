package net.pantasystem.milktea.api.misskey.users

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

class BaseUserDTOTest {

    val parser = Json {
        ignoreUnknownKeys = true
        this.useArrayPolymorphism
    }


    @Test
    fun decodeFromV13() {
        val json = """
            {
                "id": "93p88dgkeb",
                "name": "テスト:blobcatgooglypencil:",
                "username": "ringoringo",
                "host": null,
                "avatarUrl": "https://media.sushi.ski/files/thumbnail-31cd1772-507b-468d-9885-5f1f1b9fec1e.webp",
                "avatarBlurhash": "",
                "isBot": false,
                "isCat": false,
                "emojis": {},
                "onlineStatus": "online"
            }
        """.trimIndent()

        val result = parser.decodeFromString<UserDTO>(json)
    }
}