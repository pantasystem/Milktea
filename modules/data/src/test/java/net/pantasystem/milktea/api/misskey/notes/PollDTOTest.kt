package net.pantasystem.milktea.api.misskey.notes

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


class PollDTOTest {

    private val decoder by lazy {
        Json {
            ignoreUnknownKeys = true
        }
    }

    @Test
    fun decodeJsonTestGiveV10() {
        val text = """{
            "choices": [
                {
                    "id": 0,
                    "text": "ジューシーな高田馬場",
                    "votes": 1,
                    "isVoted": true
                },
                {
                    "id": 1,
                    "text": "偽のノーパソ",
                    "votes": 0
                },
                {
                    "id": 2,
                    "text": "謎のプラグイン",
                    "votes": 1
                },
                {
                    "id": 3,
                    "text": "パンドラの箱",
                    "votes": 0
                },
                {
                    "id": 4,
                    "text": "デザイナーズhub",
                    "votes": 0
                }
            ],
            "multiple": false,
            "expiresAt": "2022-07-20T07:02:17.113Z"
        }"""

        val pollDTO: PollDTO = decoder.decodeFromString(text)
        Assertions.assertEquals(false, pollDTO.multiple)

        Assertions.assertEquals("ジューシーな高田馬場", pollDTO.choices[0].text)
        Assertions.assertEquals(true, pollDTO.choices[0].isVoted)

        Assertions.assertEquals("偽のノーパソ", pollDTO.choices[1].text)
        Assertions.assertEquals(false, pollDTO.choices[1].isVoted)

    }

    @Test
    fun decodeJsonTextGiveV12() {

        val text = """
            {
            "multiple": false,
            "expiresAt": null,
            "choices": [
                {
                    "text": "a",
                    "votes": 0,
                    "isVoted": false
                },
                {
                    "text": "b",
                    "votes": 0,
                    "isVoted": false
                }
            ]
        }
        """.trimIndent()

        val dto = decoder.decodeFromString<PollDTO>(text)
        Assertions.assertEquals(false, dto.choices[0].isVoted)
        Assertions.assertEquals(false, dto.choices[1].isVoted)

        Assertions.assertEquals("a", dto.choices[0].text)
        Assertions.assertEquals("b", dto.choices[1].text)

    }

}