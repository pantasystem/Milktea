package jp.panta.misskeyandroidclient.viewmodel.notes

import jp.panta.misskeyandroidclient.model.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.core.Account
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import org.junit.Assert
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before

class TimelineStateTest {

    lateinit var misskeyAPI: MisskeyAPI
    lateinit var timelineState: TimelineState

    @Before
    fun setup(){
        misskeyAPI = MisskeyAPIServiceBuilder.build("https://misskey.io")
        val list = misskeyAPI.globalTimeline(NoteRequest()).execute().body()?.map{
            PlaneNoteViewData(it, Account("test"))
        }!!
        timelineState = TimelineState(
            list,
            TimelineState.State.INIT
        )

    }
    @Test
    fun getSinceIds() {
        val ids = timelineState.getSinceIds(3)
        assertEquals(ids.size, 3)

    }

    @Test
    fun getUntilIds() {
        val ids = timelineState.getUntilIds(3)
        assertEquals(ids.size, 3)

    }

    @Test
    fun getRequestFromUntilIds() {
        val request = TimelineViewModel.Request.makeUntilIdRequest(timelineState)
        assertNotEquals(request, null)
    }
}