package jp.panta.misskeyandroidclient.model.notes.impl


import jp.panta.misskeyandroidclient.logger.TestLogger
import jp.panta.misskeyandroidclient.model.account.TestAccountRepository
import jp.panta.misskeyandroidclient.streaming.TestSocketWithAccountProviderImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import net.pantasystem.milktea.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.api.misskey.users.UserDTO
import net.pantasystem.milktea.api_streaming.NoteCaptureAPIImpl
import net.pantasystem.milktea.api_streaming.NoteUpdated
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.data.infrastructure.notes.NoteCaptureAPIWithAccountProviderImpl
import net.pantasystem.milktea.data.infrastructure.notes.impl.InMemoryNoteDataSource
import net.pantasystem.milktea.data.infrastructure.toNote
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import net.pantasystem.milktea.model.notes.NoteDataSource
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
        noteDataSource = InMemoryNoteDataSource()
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

            val dto = NoteDTO(
                "note-1",
                Clock.System.now(),
                renoteCount = 0,
                replyCount = 0,
                userId = "hoge",
                user = UserDTO("hoge", "hogeName")
            )
            val note = dto.toNote(
                account, NodeInfo(
                    host = "", version = "", software = NodeInfo.Software(
                        name = "misskey",
                        version = ""
                    )

                )
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