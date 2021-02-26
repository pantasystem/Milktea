package jp.panta.misskeyandroidclient.model.notes.impl

import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.api.notes.toNote
import jp.panta.misskeyandroidclient.api.users.UserDTO
import jp.panta.misskeyandroidclient.logger.TestLogger
import jp.panta.misskeyandroidclient.model.account.TestAccountRepository
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.NoteRepository
import jp.panta.misskeyandroidclient.model.streming.NoteCapture
import jp.panta.misskeyandroidclient.streaming.NoteUpdated
import jp.panta.misskeyandroidclient.streaming.StreamingEvent
import jp.panta.misskeyandroidclient.streaming.TestSocketWithAccountProviderImpl
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.junit.Assert.*
import org.junit.Test
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class NoteCaptureAPIAdapterTest {

    @ExperimentalCoroutinesApi
    @Test
    fun testCapture() {
        val loggerFactory = TestLogger.Factory()
        val noteCaptureAPIWithAccountProvider = NoteCaptureAPIWithAccountProvider(
            TestSocketWithAccountProviderImpl(),
            loggerFactory
        )


        val coroutineScope = CoroutineScope(Job())
        val accountRepository = TestAccountRepository()
        val noteRepository = InMemoryNoteRepository(loggerFactory)


        val noteCaptureAPIAdapter = NoteCaptureAPIAdapter(
            accountRepository = accountRepository,
            noteRepository = noteRepository,
            noteCaptureAPIWithAccountProvider = noteCaptureAPIWithAccountProvider,
            loggerFactory,
            coroutineScope
        )

        runBlocking {

            val account = accountRepository.getCurrentAccount()
            val noteCapture = noteCaptureAPIWithAccountProvider.get(account)

            val dto = NoteDTO(
                "note-1",
                Date(),
                renoteCount = 0,
                replyCount = 0,
                userId = "hoge",
                user = UserDTO("hoge", "hogeName")
            )
            val note = dto.toNote(account)
            noteRepository.add(
                note
            )

            var sendCount = AtomicInteger()
            val job = launch {

                for(n in 0 until 10){
                    noteCaptureAPIAdapter.capture(note.id).onEach {
                        if(it is NoteRepository.Event.Updated){
                            assertTrue(it.note.reactionCounts[0].count == 1)
                        }
                    }.launchIn(this)
                }


                delay(400)
                noteCapture.handle(NoteUpdated(NoteUpdated.Body.Reacted(id = note.id.noteId, body = NoteUpdated.Body.Reacted.Body(reaction = "hoge", account.remoteId))))
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

            val job4 = launch {
                noteCaptureAPIAdapter.capture(note.id).onEach {
                    println("job4: on ev: $it")
                }.launchIn(this + Dispatchers.IO)
            }


            delay(1000)
            noteCapture.handle(NoteUpdated(NoteUpdated.Body.Reacted(id = note.id.noteId, body = NoteUpdated.Body.Reacted.Body(reaction = "hoge", account.remoteId))))
            sendCount.incrementAndGet()
            job2.cancel()
            job3.cancel()

            delay(100)

            launch {
                println("nowCount:$sendCount")
                for(n in 0 until 100) {
                    if(n % 2 == 0){
                        noteCapture.handle(NoteUpdated(NoteUpdated.Body.Reacted(id = note.id.noteId, body = NoteUpdated.Body.Reacted.Body(reaction = "hoge", account.remoteId))))
                        sendCount.incrementAndGet()
                    }else{
                        noteCapture.handle(NoteUpdated(NoteUpdated.Body.Unreacted(id = note.id.noteId, body = NoteUpdated.Body.Unreacted.Body(reaction = "hoge", account.remoteId))))
                        sendCount.decrementAndGet()
                    }

                    println("sendMsg:$sendCount, n:$n")

                }

            }

            delay(1000)
        }

    }
}