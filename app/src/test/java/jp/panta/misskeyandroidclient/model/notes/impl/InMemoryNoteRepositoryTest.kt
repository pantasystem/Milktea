package jp.panta.misskeyandroidclient.model.notes.impl

import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.api.notes.toNote
import jp.panta.misskeyandroidclient.api.users.UserDTO
import jp.panta.misskeyandroidclient.logger.TestLogger
import jp.panta.misskeyandroidclient.model.AddResult
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.NoteRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.util.*

class InMemoryNoteRepositoryTest {

    @Test
    fun testAdd() {
        val loggerFactory = TestLogger.Factory()
        val noteRepository = InMemoryNoteRepository(loggerFactory)

        val account = Account(remoteId = "piyo", instanceDomain = "", encryptedToken = "", userName = "piyoName")
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
            val result = noteRepository.add(
                note
            )

            assertTrue(result == AddResult.CREATED)
            val old = note.copy()

            assertTrue(noteRepository.add(note) == AddResult.UPDATED)

            assertTrue(noteRepository.add(old) == AddResult.CANCEL)

            assertTrue(noteRepository.add(old.copy()) == AddResult.CANCEL)
        }

    }
}