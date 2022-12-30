package jp.panta.misskeyandroidclient.model.account.page

import net.pantasystem.milktea.data.infrastructure.notes.toNoteRequest
import net.pantasystem.milktea.model.account.page.PageType
import net.pantasystem.milktea.model.account.page.Pageable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test



class PageableChannelTimelineTest {

    @Test
    fun toParams() {
        val pageable = Pageable.ChannelTimeline(channelId = "channelId")
        assertNotNull(pageable.toParams().channelId)
        assertEquals("channelId", pageable.toParams().channelId)
        assertEquals(PageType.CHANNEL_TIMELINE, pageable.toParams().type)
    }

    @Test
    fun makeNoteRequest() {
        val pageable = Pageable.ChannelTimeline(channelId = "channelId")
        val request = pageable.toParams().toNoteRequest("test")
        assertNotNull(request.channelId)
        assertEquals("channelId", request.channelId)
    }
}