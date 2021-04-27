package jp.panta.misskeyandroidclient.model.notes.impl

import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.api.notes.toNote
import jp.panta.misskeyandroidclient.api.users.UserDTO
import jp.panta.misskeyandroidclient.logger.TestLogger
import jp.panta.misskeyandroidclient.model.AddResult
import jp.panta.misskeyandroidclient.model.account.Account
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*

class InMemoryNoteDataSourceTest {

    lateinit var loggerFactory: Logger.Factory
    lateinit var account: Account
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
            Date(),
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

            assertTrue(result == AddResult.CREATED)
            val old = note.copy()

            assertTrue(noteDataSource.add(note) == AddResult.UPDATED)

            assertTrue(noteDataSource.add(old) == AddResult.CANCEL)

            assertTrue(noteDataSource.add(old.copy()) == AddResult.CANCEL)
        }

    }


    @Test
    fun testUpdateNote(): Unit = runBlocking{
        val noteDataSource = InMemoryNoteDataSource(loggerFactory)

        val dto = NoteDTO(
            "note-1",
            Date(),
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