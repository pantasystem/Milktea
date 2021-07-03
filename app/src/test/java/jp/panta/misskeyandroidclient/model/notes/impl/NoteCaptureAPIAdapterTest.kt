package jp.panta.misskeyandroidclient.model.notes.impl

import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.api.notes.toNote
import jp.panta.misskeyandroidclient.api.users.UserDTO
import jp.panta.misskeyandroidclient.logger.TestLogger
import jp.panta.misskeyandroidclient.model.AddResult
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.model.account.TestAccountRepository
import jp.panta.misskeyandroidclient.model.notes.NoteCaptureAPIAdapter
import jp.panta.misskeyandroidclient.model.notes.NoteCaptureAPIWithAccountProvider
import jp.panta.misskeyandroidclient.model.notes.NoteCaptureAPIWithAccountProviderImpl
import jp.panta.misskeyandroidclient.model.notes.NoteDataSource
import jp.panta.misskeyandroidclient.streaming.NoteUpdated
import jp.panta.misskeyandroidclient.streaming.TestSocketWithAccountProviderImpl
import jp.panta.misskeyandroidclient.streaming.notes.NoteCaptureAPI
import jp.panta.misskeyandroidclient.streaming.notes.NoteCaptureAPIImpl
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.Clock
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class NoteCaptureAPIAdapterTest {

    lateinit var loggerFactory: Logger.Factory
    lateinit var accountRepository: AccountRepository
    lateinit var noteDataSource: NoteDataSource

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


        val coroutineScope = CoroutineScope(Job())


        val noteCaptureAPIAdapter = NoteCaptureAPIAdapter(
            accountRepository = accountRepository,
            noteDataSource = noteDataSource,
            noteCaptureAPIWithAccountProvider = noteCaptureAPIWithAccountProvider,
            loggerFactory,
            coroutineScope
        )

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

            val sendCount = AtomicInteger()
            val job = launch {

                for(n in 0 until 10){
                    noteCaptureAPIAdapter.capture(note.id).onEach {
                        if(it is NoteDataSource.Event.Updated){
                            assertTrue(it.note.reactionCounts[0].count == 1)
                        }
                    }.launchIn(this)
                }


                delay(400)
                noteCapture.onMessage(NoteUpdated(NoteUpdated.Body.Reacted(id = note.id.noteId, body = NoteUpdated.Body.Reacted.Body(reaction = "hoge", account.remoteId))))
                sendCount.incrementAndGet()

            }

            delay(2000)
            assertTrue(true)
            job.cancel()

            val job2 = launch {
                noteCaptureAPIAdapter.capture(note.id).onEach {
                    println("job2: on ev")
                }.launchIn(this + Dispatchers.IO)
            }

            val job3 = launch {
                noteCaptureAPIAdapter.capture(note.id).onEach {
                    println("job3: on ev")
                }.launchIn(this + Dispatchers.IO)
            }

            launch {
                noteCaptureAPIAdapter.capture(note.id).onEach {
                    println("job4: on ev: $it")
                }.launchIn(this + Dispatchers.IO)
            }


            delay(1000)
            noteCapture.onMessage(NoteUpdated(NoteUpdated.Body.Reacted(id = note.id.noteId, body = NoteUpdated.Body.Reacted.Body(reaction = "hoge", account.remoteId))))
            sendCount.incrementAndGet()
            job2.cancel()
            job3.cancel()

            delay(100)

            launch {
                println("nowCount:$sendCount")
                for(n in 0 until 100) {
                    if(n % 2 == 0){
                        noteCapture.onMessage(NoteUpdated(NoteUpdated.Body.Reacted(id = note.id.noteId, body = NoteUpdated.Body.Reacted.Body(reaction = "hoge", account.remoteId))))
                        sendCount.incrementAndGet()
                    }else{
                        noteCapture.onMessage(NoteUpdated(NoteUpdated.Body.Unreacted(id = note.id.noteId, body = NoteUpdated.Body.Unreacted.Body(reaction = "hoge", account.remoteId))))
                        sendCount.decrementAndGet()
                    }

                    println("sendMsg:$sendCount, n:$n")

                }

            }

            delay(1000)
        }

    }
}