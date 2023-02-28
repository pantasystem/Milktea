package jp.panta.misskeyandroidclient.model.notes.impl


import jp.panta.misskeyandroidclient.logger.TestLogger
import jp.panta.misskeyandroidclient.model.account.TestAccountRepository
import jp.panta.misskeyandroidclient.streaming.TestSocketWithAccountProviderImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import net.pantasystem.milktea.api_streaming.NoteCaptureAPIImpl
import net.pantasystem.milktea.api_streaming.NoteUpdated
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.data.infrastructure.MemoryCacheCleaner
import net.pantasystem.milktea.data.infrastructure.notes.NoteCaptureAPIWithAccountProviderImpl
import net.pantasystem.milktea.data.infrastructure.notes.impl.InMemoryNoteDataSource
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteDataSource
import net.pantasystem.milktea.model.notes.make
import net.pantasystem.milktea.model.user.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class NoteCaptureAPIAdapterTest {

    private lateinit var loggerFactory: Logger.Factory
    private lateinit var accountRepository: AccountRepository
    private lateinit var noteDataSource: NoteDataSource

    @BeforeEach
    fun setUp() {
        loggerFactory = TestLogger.Factory()
        accountRepository = TestAccountRepository()
        noteDataSource = InMemoryNoteDataSource(MemoryCacheCleaner())
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testCapture() {
        val noteCaptureAPIWithAccountProvider =
            NoteCaptureAPIWithAccountProviderImpl(
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

            val account = accountRepository.getCurrentAccount().getOrThrow()
            val noteCapture = noteCaptureAPIWithAccountProvider.get(account) as NoteCaptureAPIImpl

            val note = Note.make(
                id = Note.Id(account.accountId, "note-1"),
                userId = User.Id(account.accountId, "hoge")
            )
            noteDataSource.add(
                note
            )

            var counter = 1
            noteDataSource.addEventListener {
                if (it.noteId == note.id) {
                    assertEquals(
                        1,
                        (it as NoteDataSource.Event.Updated).note.reactionCounts[0].count
                    )
                    counter++
                }
            }

            noteCapture.onMessage(
                NoteUpdated(
                    NoteUpdated.Body.Reacted(
                        id = note.id.noteId,
                        body = NoteUpdated.Body.Reacted.Body(
                            reaction = "hoge",
                            account.remoteId
                        )
                    )
                )
            )
            noteCapture.onMessage(
                NoteUpdated(
                    NoteUpdated.Body.Reacted(
                        id = note.id.noteId,
                        body = NoteUpdated.Body.Reacted.Body(
                            reaction = "hoge",
                            account.remoteId
                        )
                    )
                )
            )
            noteCapture.onMessage(
                NoteUpdated(
                    NoteUpdated.Body.Reacted(
                        id = note.id.noteId,
                        body = NoteUpdated.Body.Reacted.Body(
                            reaction = "hoge",
                            account.remoteId
                        )
                    )
                )
            )


        }

    }
}