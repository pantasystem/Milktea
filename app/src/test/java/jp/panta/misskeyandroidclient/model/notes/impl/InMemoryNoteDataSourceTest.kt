package jp.panta.misskeyandroidclient.model.notes.impl

import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.api.notes.toNote
import jp.panta.misskeyandroidclient.api.users.UserDTO
import jp.panta.misskeyandroidclient.logger.TestLogger
import jp.panta.misskeyandroidclient.model.AddResult
import jp.panta.misskeyandroidclient.model.account.Account
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*

class InMemoryNoteDataSourceTest {

    private lateinit var loggerFactory: Logger.Factory
    private lateinit var account: Account
    @Before
    fun setUp() {
        loggerFactory = TestLogger.Factory()
        account = Account(remoteId = "piyo", instanceDomain = "", encryptedToken = "", userName = "piyoName")
    }
    @Test
    fun testAdd() {
        val noteDataSource = InMemoryNoteDataSource(loggerFactory)

        val dto = NoteDTO(
            "",
            Clock.System.now(),
            renoteCount = 0,
            replyCount = 0,
            userId = "hoge",
            user = UserDTO("hoge", "hogeName")
        )
        val note = dto.toNote(account)
        runBlocking {
            val result = noteDataSource.add(
                note
            )
            delay(10)

            assertEquals(AddResult.CREATED, result)
            val old = note.copy()
            delay(10)

            assertEquals(AddResult.UPDATED, noteDataSource.add(note))
            delay(10)

            assertEquals(AddResult.CANCEL, noteDataSource.add(old))
            delay(10)

            assertEquals(AddResult.CANCEL, noteDataSource.add(old.copy()))
        }

    }


    @Test
    fun testUpdateNote(): Unit = runBlocking{
        val noteDataSource = InMemoryNoteDataSource(loggerFactory)

        val dto = NoteDTO(
            "note-1",
            Clock.System.now(),
            renoteCount = 0,
            replyCount = 0,
            userId = "hoge",
            user = UserDTO("hoge", "hogeName")
        )
        val note = dto.toNote(account)
        noteDataSource.add(
            note
        )

        val dtoParsed = dto.toNote(account)
        assertEquals(AddResult.UPDATED, noteDataSource.add(dtoParsed))

        assertTrue(dtoParsed === noteDataSource.get(dtoParsed.id))
    }
}