package net.pantasystem.milktea.data.infrastructure.emoji

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import net.pantasystem.milktea.data.infrastructure.DataBase
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class Utf8EmojiRepositoryImplTest {

    lateinit var database: DataBase

    @Before
    fun setup() {

        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, DataBase::class.java).build()
    }

    @Test
    fun exists() {
        val job = Job()
        val utf8EmojiRepositoryImpl = Utf8EmojiRepositoryImpl(
                CoroutineScope(job),
                null,
                Dispatchers.Default,
                database.utf8EmojiDAO()
            )
        runBlocking {
            Assert.assertNotEquals(0, utf8EmojiRepositoryImpl.findAll().size)
            Assert.assertTrue(setOf("㊙️", "㊙︎").contains("㊙︎"))


            Assert.assertTrue(utf8EmojiRepositoryImpl.exists("㊙️"))
            Assert.assertTrue(utf8EmojiRepositoryImpl.exists("㊙︎"))
            Assert.assertTrue(utf8EmojiRepositoryImpl.exists("‼︎"))
            Assert.assertTrue(utf8EmojiRepositoryImpl.exists("‼️"))
            Assert.assertTrue(utf8EmojiRepositoryImpl.exists("♀"))
            Assert.assertFalse(utf8EmojiRepositoryImpl.exists("あ"))
            Assert.assertFalse(utf8EmojiRepositoryImpl.exists("a"))
            Assert.assertFalse(utf8EmojiRepositoryImpl.exists("c"))
            Assert.assertFalse(utf8EmojiRepositoryImpl.exists("1"))
            Assert.assertFalse(utf8EmojiRepositoryImpl.exists("2"))
            Assert.assertFalse(utf8EmojiRepositoryImpl.exists(" "))

            Assert.assertFalse(utf8EmojiRepositoryImpl.exists("3"))
            Assert.assertTrue(utf8EmojiRepositoryImpl.exists("\uD83E\uDD7A"))
            Assert.assertFalse(utf8EmojiRepositoryImpl.exists(" harunon "))
            Assert.assertFalse(utf8EmojiRepositoryImpl.exists("鶏"))
            Assert.assertTrue(utf8EmojiRepositoryImpl.exists("☕️"))
            Assert.assertTrue(utf8EmojiRepositoryImpl.exists("\uD83D\uDCA2"))


        }

        job.cancel()
    }
}