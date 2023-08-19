import net.pantasystem.milktea.data.infrastructure.note.draft.db.DraftNoteDTO
import net.pantasystem.milktea.data.infrastructure.note.draft.db.DraftPollDTO
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.note.draft.DraftNote
import org.junit.jupiter.api.Assertions
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
        Assertions.assertNotNull(note.channelId)
        Assertions.assertNotNull(dto.channelId)
        Assertions.assertEquals(note.channelId?.channelId, dto.channelId)
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
            visibility = "public",
        )
        val entity = dto.toDraftNote(1L, null, null, null)
        Assertions.assertEquals(1L, entity.accountId)
        Assertions.assertEquals(dto.channelId, entity.channelId?.channelId)
        Assertions.assertEquals(dto.cw, entity.cw)
        Assertions.assertEquals(dto.text, entity.text)
        Assertions.assertEquals(dto.renoteId, entity.renoteId)
        Assertions.assertEquals(dto.replyId, entity.replyId)
        Assertions.assertEquals(dto.visibility, entity.visibility)
    }
}