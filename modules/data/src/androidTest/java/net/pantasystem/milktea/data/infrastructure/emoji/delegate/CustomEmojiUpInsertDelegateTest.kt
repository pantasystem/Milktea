package net.pantasystem.milktea.data.infrastructure.emoji.delegate

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import net.pantasystem.milktea.data.infrastructure.DataBase
import net.pantasystem.milktea.data.infrastructure.emoji.db.CustomEmojiAliasRecord
import net.pantasystem.milktea.data.infrastructure.emoji.db.CustomEmojiDAO
import net.pantasystem.milktea.data.infrastructure.emoji.db.toRecord
import net.pantasystem.milktea.model.emoji.Emoji
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class CustomEmojiUpInsertDelegateTest {

    private lateinit var dao: CustomEmojiDAO

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val database = Room.inMemoryDatabaseBuilder(context, DataBase::class.java).build()
        dao = database.customEmojiDao()
    }

    @Test
    fun giveNotExistsData() = runBlocking {
        val emojis = listOf(
            Emoji(
                name = "test1",
                aliases = listOf("a", "b", "c")
            ),
            Emoji(
                name = "test2",
                aliases = listOf("a2`", "b2", "c2")
            ),
            Emoji(
                name = "test3",
                aliases = listOf("a3", "b3", "c3", "d4")
            ),
            Emoji(
                name = "test4",
                aliases = listOf("")
            )
        )

        val delegate = CustomEmojiUpInsertDelegate(dao)
        delegate("misskey.pantasystem.com", emojis)

        val actual = dao.findBy("misskey.pantasystem.com").map {
            it.toModel()
        }

        val expect = emojis.map { emoji ->
            emoji.copy(
                aliases = emoji.aliases?.filterNot {
                    it.isBlank()
                }
            )
        }
        Assert.assertEquals(
            expect,
            actual
        )
    }

    @Test
    fun giveExistsData() = runBlocking {
        val existsData = listOf(
            Emoji(
                name = "test1",
                aliases = listOf("a", "b", "c")
            ),
            Emoji(
                name = "test2",
                aliases = listOf("a2`", "b2", "c2")
            ),
        )

        val emojis = listOf(
            Emoji(
                name = "test1",
                aliases = listOf("a", "b", "c")
            ),
            Emoji(
                name = "test2",
                aliases = listOf("a2`", "b2", "c2", "updated-alias")
            ),
            Emoji(
                name = "test3",
                aliases = listOf("a3", "b3", "c3", "d4")
            ),
            Emoji(
                name = "test4",
                aliases = listOf("")
            )
        )

        val ids = dao.insertAll(existsData.map { it.toRecord("misskey.pantasystem.com") })
        ids.mapIndexed { index, l ->
            existsData[index].aliases?.map {
                CustomEmojiAliasRecord(l, it)
            }?.let {
                dao.insertAliases(it)
            }

        }

        val delegate = CustomEmojiUpInsertDelegate(dao)
        delegate("misskey.pantasystem.com", emojis)

        val actual = dao.findBy("misskey.pantasystem.com").map {
            it.toModel()
        }

        val expect = emojis.map { emoji ->
            emoji.copy(
                aliases = emoji.aliases?.filterNot {
                    it.isBlank()
                }
            )
        }
        Assert.assertEquals(
            expect,
            actual
        )
    }

}