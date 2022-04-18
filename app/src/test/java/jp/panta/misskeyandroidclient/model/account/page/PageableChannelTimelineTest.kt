package jp.panta.misskeyandroidclient.model.account.page

import net.pantasystem.milktea.model.account.page.PageType
import net.pantasystem.milktea.model.account.page.Pageable
import org.junit.Assert.*

import org.junit.Test

class PageableChannelTimelineTest {

    @Test
    fun toParams() {
        val pageable = net.pantasystem.milktea.model.account.page.Pageable.ChannelTimeline(channelId = "channelId")
        assertNotNull(pageable.toParams().channelId)
        assertEquals("channelId", pageable.toParams().channelId)
        assertEquals(net.pantasystem.milktea.model.account.page.PageType.CHANNEL_TIMELINE, pageable.toParams().type)
    }

    @Test
    fun makeNoteRequest() {
        val pageable = net.pantasystem.milktea.model.account.page.Pageable.ChannelTimeline(channelId = "channelId")
        val request = pageable.toParams().toNoteRequest("test")
        assertNotNull(request.channelId)
        assertEquals("channelId", request.channelId)
    }
}