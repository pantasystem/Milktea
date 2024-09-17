package net.pantasystem.milktea.data.infrastructure.note.timeline

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import net.pantasystem.milktea.data.infrastructure.DataBase
import net.pantasystem.milktea.data.infrastructure.note.impl.sqlite.NoteDAO
import net.pantasystem.milktea.data.infrastructure.note.impl.sqlite.NoteEntity
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.make
import net.pantasystem.milktea.model.user.User
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TimelineCacheDAOTest {

    private lateinit var timelineCacheDAO: TimelineCacheDAO
    private lateinit var noteDAO: NoteDAO
    private lateinit var db: DataBase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, DataBase::class.java).build()
        timelineCacheDAO = db.timelineCacheDAO()

        noteDAO = db.noteDAO()
    }

    @Test
    fun getTimelineItemsSinceId() = runTest {
        val accountId = 1L
        val pageId = 1L
        timelineCacheDAO.clear(accountId, pageId)
        val testData = (1 until 1000).map {
            val noteId = it.toString().padStart(4, '0')
            Assert.assertEquals(4, noteId.length)
            TimelineItemEntity(
                accountId = accountId,
                pageId = pageId,
                noteId = noteId,
                noteLocalId = NoteEntity.makeEntityId(
                    Note.Id(accountId, noteId)
                ),
                id = 0
            )
        }
        noteDAO.insertAll(
            testData.map {
                NoteEntity.fromModel(Note.make(
                    Note.Id(it.accountId, it.noteId),
                    User.Id(it.accountId, "user")
                ))
            }
        )
        timelineCacheDAO.insertAll(testData)
        Assert.assertEquals(
            testData.size.toLong(), noteDAO.count()
        )
        Assert.assertEquals(
            testData.size.toLong(),
            timelineCacheDAO.count()
        )
        val items = timelineCacheDAO.getTimelineItemsSinceId(
            accountId = accountId,
            pageId = pageId,
            sinceId = "0400",
            limit = 10,
        )
        Assert.assertTrue(items.isNotEmpty())
        Assert.assertEquals(
            "0401",
            items[0].noteId,
        )
        Assert.assertEquals(
            "0402",
            items[1].noteId,
        )
        println(items)
        Assert.assertEquals(
            "0410",
            items[9].noteId,
        )
    }

    @Test
    fun getTimelineItemsUntilId() = runTest {
        val accountId = 1L
        val pageId = 1L
        timelineCacheDAO.clear(accountId, pageId)
        val testData = (1 until 1000).map {
            val noteId = it.toString().padStart(4, '0')
            Assert.assertEquals(4, noteId.length)
            TimelineItemEntity(
                accountId = accountId,
                pageId = pageId,
                noteId = noteId,
                noteLocalId = NoteEntity.makeEntityId(
                    Note.Id(accountId, noteId)
                ),
                id = 0
            )
        }
        noteDAO.insertAll(
            testData.map {
                NoteEntity.fromModel(Note.make(
                    Note.Id(it.accountId, it.noteId),
                    User.Id(it.accountId, "user")
                ))
            }
        )
        timelineCacheDAO.insertAll(testData)
        Assert.assertEquals(
            testData.size.toLong(), noteDAO.count()
        )
        Assert.assertEquals(
            testData.size.toLong(),
            timelineCacheDAO.count()
        )
        val items = timelineCacheDAO.getTimelineItemsUntilId(
            accountId = accountId,
            pageId = pageId,
            untilId = "0400",
            limit = 10,
        )
        Assert.assertTrue(items.isNotEmpty())
        Assert.assertEquals(
            "0399",
            items[0].noteId,
        )
        Assert.assertEquals(
            "0398",
            items[1].noteId,
        )
        println(items)
        Assert.assertEquals(
            "0390",
            items[9].noteId,
        )
    }

    @Test
    fun getTimelineItems() = runTest {
        val accountId = 1L
        val pageId = 1L
        timelineCacheDAO.clear(accountId, pageId)
        val testData = (1 until 1000).map {
            val noteId = it.toString().padStart(4, '0')
            Assert.assertEquals(4, noteId.length)
            TimelineItemEntity(
                accountId = accountId,
                pageId = pageId,
                noteId = noteId,
                noteLocalId = NoteEntity.makeEntityId(
                    Note.Id(accountId, noteId)
                ),
                id = 0
            )
        }
        noteDAO.insertAll(
            testData.map {
                NoteEntity.fromModel(Note.make(
                    Note.Id(it.accountId, it.noteId),
                    User.Id(it.accountId, "user")
                ))
            }
        )
        timelineCacheDAO.insertAll(testData)
        Assert.assertEquals(
            testData.size.toLong(), noteDAO.count()
        )
        Assert.assertEquals(
            testData.size.toLong(),
            timelineCacheDAO.count()
        )
        val items = timelineCacheDAO.getTimelineItems(
            accountId = accountId,
            pageId = pageId,
            limit = 10,
        )
        Assert.assertTrue(items.isNotEmpty())
        Assert.assertEquals(
            "0999",
            items[0].noteId,
        )
        Assert.assertEquals(
            "0998",
            items[1].noteId,
        )
        println(items)
        Assert.assertEquals(
            "0990",
            items[9].noteId,
        )
    }


    @After
    fun after() {
        db.close()
    }
}