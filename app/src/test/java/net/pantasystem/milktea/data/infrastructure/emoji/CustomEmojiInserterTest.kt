package net.pantasystem.milktea.data.infrastructure.emoji

import kotlinx.coroutines.test.runTest
import net.pantasystem.milktea.model.emoji.CustomEmoji
import net.pantasystem.milktea.model.emoji.EmojiWithAlias
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyBlocking

class CustomEmojiInserterTest {

    @Test
    fun convertAndReplaceAll() = runTest {
        val dao = mock<CustomEmojiDAO>() {
            onBlocking {
                insertAll(any())
            } doReturn listOf(1L, 2L, 3L)
            onBlocking {
                deleteByHost(any())
            } doReturn Unit
            onBlocking {
                insertAliases(
                    any(),
                )
            } doReturn listOf(1L, 2L, 3L)
        }
        val inserter = CustomEmojiInserter(
            dao
        )

        val remoteEmojis = listOf(
            EmojiWithAlias(
                emoji = CustomEmoji(
                    name = "name",
                ),
                aliases = listOf(
                    "alias1",
                    "alias2",
                )
            ),
            EmojiWithAlias(
                emoji = CustomEmoji(
                    name = "name1",
                ),
                aliases = listOf(
                    "alias3",
                    "alias4",
                )
            ),
            EmojiWithAlias(
                emoji = CustomEmoji(
                    name = "name2",
                ),
                aliases = listOf(
                    "alias5",
                    "alias6",
                )
            )
        )

        inserter.convertAndReplaceAll("host", remoteEmojis)
        verifyBlocking(dao) {
            deleteByHost("host")
            insertAll(
                remoteEmojis.map {
                    CustomEmojiRecord.from(
                        it.emoji,
                        "host",
                    )
                }
            )
            insertAliases(
                listOf(
                    CustomEmojiAliasRecord(
                        "alias1",
                        1L,
                    ),
                    CustomEmojiAliasRecord(
                        "alias2",
                        1L,
                    ),
                    CustomEmojiAliasRecord(
                        "alias3",
                        2L,
                    ),
                    CustomEmojiAliasRecord(
                        "alias4",
                        2L,
                    ),
                    CustomEmojiAliasRecord(
                        "alias5",
                        3L,
                    ),
                    CustomEmojiAliasRecord(
                        "alias6",
                        3L,
                    ),
                )
            )
        }
    }

    @Test
    fun replaceAll() = runTest {
        val dao = mock<CustomEmojiDAO>() {
            onBlocking {
                insertAll(any())
            } doReturn listOf(1L, 2L, 3L)
            onBlocking {
                deleteByHost(any())
            } doReturn Unit
        }
        val inserter = CustomEmojiInserter(
            dao
        )
        val records = listOf(
            CustomEmojiRecord(
                name = "name",
                emojiHost = "host",
                url = "url",
                uri = "uri",
                type = "type",
                serverId = "serverId",
                category = "category",
                id = 0L,
            ),
            CustomEmojiRecord(
                name = "name1",
                emojiHost = "host",
                url = "url",
                uri = "uri",
                type = "type",
                serverId = "serverId",
                category = "category",
                id = 0L,
            ),
            CustomEmojiRecord(
                name = "name2",
                emojiHost = "host",
                url = "url",
                uri = "uri",
                type = "type",
                serverId = "serverId",
                category = "category",
                id = 0L,
            )
        )
        inserter.replaceAll(
            "host",
            records,
        )
        verifyBlocking(dao) {
            deleteByHost("host")
            insertAll(records)
        }
    }

    @Test
    fun inLock() = runTest {
        val inserter = CustomEmojiInserter(
            mock()
        )
        inserter.inLock("host") {
            Assertions.assertNotNull(inserter.locks["host"])
            Assertions.assertEquals(inserter.locks["host"]?.isLocked, true)
        }
        Assertions.assertNotNull(inserter.locks["host"])
        Assertions.assertEquals(inserter.locks["host"]?.isLocked, false)
    }
}