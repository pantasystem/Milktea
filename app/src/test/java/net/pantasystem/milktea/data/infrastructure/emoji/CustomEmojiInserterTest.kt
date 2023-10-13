package net.pantasystem.milktea.data.infrastructure.emoji

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyBlocking

class CustomEmojiInserterTest {

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