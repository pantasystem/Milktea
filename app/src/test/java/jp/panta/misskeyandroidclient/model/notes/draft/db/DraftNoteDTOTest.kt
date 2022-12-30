package jp.panta.misskeyandroidclient.model.notes.draft.db

import net.pantasystem.milktea.data.infrastructure.notes.draft.db.DraftNoteDTO
import net.pantasystem.milktea.data.infrastructure.notes.draft.db.DraftPollDTO
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.notes.draft.DraftNote
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


class DraftNoteDTOTest {

    @Test
    fun makeChannelIdParamTest() {
        val note = DraftNote(
            accountId = 0,
            text = null,
            channelId = Channel.Id(0, "id")
        )
        val dto = DraftNoteDTO.make(note)
        assertNotNull(note.channelId)
        assertNotNull(dto.channelId)
        assertEquals(note.channelId?.channelId, dto.channelId)
    }

    @Test
    fun toDraftNoteTest() {
        val dto = DraftNoteDTO(
            accountId = 1,
            poll = DraftPollDTO(
                multiple = true,
            ),
            channelId = "channelId",
            cw = "cw_test",
            text = "text_test",
            renoteId = "renote_id_test",
            replyId = "reply_id_test",
            visibility = "public"
        )
        val entity = dto.toDraftNote(1L, null, null, null)
        assertEquals(1L, entity.accountId)
        assertEquals(dto.channelId, entity.channelId?.channelId)
        assertEquals(dto.cw, entity.cw)
        assertEquals(dto.text, entity.text)
        assertEquals(dto.renoteId, entity.renoteId)
        assertEquals(dto.replyId, entity.replyId)
        assertEquals(dto.visibility, entity.visibility)
    }
}