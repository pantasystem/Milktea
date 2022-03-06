package jp.panta.misskeyandroidclient.model.notes.impl

import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.api.misskey.notes.NoteDTO
import jp.panta.misskeyandroidclient.api.misskey.notes.toNote
import jp.panta.misskeyandroidclient.api.misskey.users.UserDTO
import jp.panta.misskeyandroidclient.logger.TestLogger
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.model.account.TestAccountRepository
import jp.panta.misskeyandroidclient.model.notes.NoteCaptureAPIWithAccountProviderImpl
import jp.panta.misskeyandroidclient.model.notes.NoteDataSource
import jp.panta.misskeyandroidclient.streaming.NoteUpdated
import jp.panta.misskeyandroidclient.streaming.TestSocketWithAccountProviderImpl
import jp.panta.misskeyandroidclient.streaming.notes.NoteCaptureAPIImpl
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class NoteCaptureAPIAdapterTest {

    private lateinit var loggerFactory: Logger.Factory
    private lateinit var accountRepository: AccountRepository
    private lateinit var noteDataSource: NoteDataSource

    @Before
    fun setUp() {
        loggerFactory = TestLogger.Factory()
        accountRepository = TestAccountRepository()
        noteDataSource = InMemoryNoteDataSource(loggerFactory)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testCapture() {
        val noteCaptureAPIWithAccountProvider = NoteCaptureAPIWithAccountProviderImpl(
            TestSocketWithAccountProviderImpl(),
            loggerFactory
        )


//        val coroutineScope = CoroutineScope(Job())


//        val noteCaptureAPIAdapter = NoteCaptureAPIAdapter(
//            accountRepository = accountRepository,
//            noteDataSource = noteDataSource,
//            noteCaptureAPIWithAccountProvider = noteCaptureAPIWithAccountProvider,
//            loggerFactory,
//            coroutineScope
//        )

        runBlocking {

            val account = accountRepository.getCurrentAccount()
            val noteCapture = noteCaptureAPIWithAccountProvider.get(account) as NoteCaptureAPIImpl

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

            var counter = 1
            noteDataSource.addEventListener {
                if (it.noteId == note.id) {
                    assertEquals(1, (it as NoteDataSource.Event.Updated).note.reactionCounts[0].count)
                    counter ++
                }
            }

            noteCapture.onMessage(NoteUpdated(NoteUpdated.Body.Reacted(id = note.id.noteId, body = NoteUpdated.Body.Reacted.Body(reaction = "hoge", account.remoteId))))
            noteCapture.onMessage(NoteUpdated(NoteUpdated.Body.Reacted(id = note.id.noteId, body = NoteUpdated.Body.Reacted.Body(reaction = "hoge", account.remoteId))))
            noteCapture.onMessage(NoteUpdated(NoteUpdated.Body.Reacted(id = note.id.noteId, body = NoteUpdated.Body.Reacted.Body(reaction = "hoge", account.remoteId))))


        }

    }
}