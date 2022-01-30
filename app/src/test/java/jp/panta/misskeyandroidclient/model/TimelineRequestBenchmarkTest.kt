package jp.panta.misskeyandroidclient.model

import jp.panta.misskeyandroidclient.api.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.api.MisskeyAPI
import jp.panta.misskeyandroidclient.api.notes.NoteRequest
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.*

@RunWith(JUnit4::class)
class TimelineRequestBenchmarkTest {

    private lateinit var misskeyAPI: MisskeyAPI

    @Before
    fun setUp(){
        misskeyAPI = MisskeyAPIServiceBuilder.build("https://misskey.io")
    }
    @Test
    suspend fun beforeShowNoteTest(){
        val time = Date().time
        val noteId = "8915y6o8w6"
        val res = misskeyAPI.showNote(
            NoteRequest(
                noteId = noteId
            )
        )
        println("code:${res.code()}")

        val midTime = Date().time

        misskeyAPI.globalTimeline(
            NoteRequest(
                untilId = noteId
            )
        )

        val end = Date().time

        println("showを入れた時間:${end - time}")
        println("showを含めない時間:${end - midTime}")



    }
}