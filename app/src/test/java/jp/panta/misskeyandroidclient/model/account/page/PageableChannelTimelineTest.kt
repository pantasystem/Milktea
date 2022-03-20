package jp.panta.misskeyandroidclient.model.account.page

import org.junit.Assert.*

import org.junit.Test

class PageableChannelTimelineTest {

    @Test
    fun toParams() {
        val pageable = Pageable.ChannelTimeline(channelId = "channelId")
        assertNotNull(pageable.toParams().channelId)
        assertEquals("channelId", pageable.toParams().channelId)
    }

    @Test
    fun makeNoteRequest() {
        val pageable = Pageable.ChannelTimeline(channelId = "channelId")
        val request = pageable.toParams().toNoteRequest("test")
        assertNotNull(request.channelId)
        assertEquals("channelId", request.channelId)
    }
}