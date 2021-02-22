package jp.panta.misskeyandroidclient.streaming

import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.api.users.UserDTO
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test
import java.util.*

class StreamingEventTest {

    @Test
    fun testParseReceiveNote() {

        val noteDTO = NoteDTO(
            "piyo22",
            Date(),
            "hogehoge",
            null,
            "piyo",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            0,
            null,
            null,
            0,
            user = UserDTO(
                "piyo",
                "",
                "",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                isCat = false,
                pinnedNoteIds = null,
                pinnedNotes = null,
                twoFactorEnabled = null,
                isAdmin = null,
                avatarUrl = null,
                bannerUrl = null,
                emojis = null,
                isFollowing = null,
                isFollowed = null,
                isBlocking = null,
                isMuted = null,
                url = null
            ),
            null,
            null,
            null,
            null,
            null,
            null,
            null


        )
        val se: StreamingEvent = ChannelEvent(ChannelBody.ReceiveNote(id = "hoge", body = noteDTO))
        println(Json.encodeToString(se))
        assertTrue(true)
    }
}