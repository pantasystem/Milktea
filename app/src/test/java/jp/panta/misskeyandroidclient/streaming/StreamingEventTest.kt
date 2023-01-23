package jp.panta.misskeyandroidclient.streaming

import kotlinx.datetime.Clock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.pantasystem.milktea.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.api.misskey.users.UserDTO
import net.pantasystem.milktea.api_streaming.ChannelBody
import net.pantasystem.milktea.api_streaming.ChannelEvent
import net.pantasystem.milktea.api_streaming.StreamingEvent
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test


class StreamingEventTest {

    @Test
    fun testParseReceiveNote() {

        val noteDTO = NoteDTO(
            "piyo22",
            Clock.System.now(),
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
        val se: StreamingEvent =
            ChannelEvent(
                ChannelBody.ReceiveNote(
                    id = "hoge",
                    body = noteDTO
                )
            )
        println(Json.encodeToString(se))
        assertTrue(true)
    }

    @Test
    fun testDecodeJson() {
        val response = Json {
            ignoreUnknownKeys = true
        }
        val json = """{"type":"channel","body":{"id":"1","type":"note","body":{"id":"8ikcfyi666","createdAt":"2021-02-22T16:13:38.286Z","userId":"8gs6brks69","user":{"id":"8gs6brks69","name":"輩原にする","username":"nisuru","host":"misskey.haibala.com","avatarUrl":"https://nos3.arkjp.net/?url=https%3A%2F%2Fmisskey-oj.ewr1.vultrobjects.com%2Fm%2Fwebpublic-718e33cf-0822-477d-a801-54cdcef1327d.jpg&thumbnail=1","avatarBlurhash":"yGP@6Fxa%f?a^J?GM|xbkDaK%gWY-5W=RPM|kX_NIqVrt3WBR*RlR-oe%4Myt6xWt3t8M}Rij@n}WYbJj[R.","avatarColor":null,"isCat":true,"instance":{"name":"haibaland","softwareName":"misskey","softwareVersion":"12.71.0","iconUrl":"https://misskey.haibala.com/assets/icons/192.png","faviconUrl":"https://misskey-oj.ewr1.vultrobjects.com/m/902557d0-1ca6-4568-b92a-eb6e195d0228.png","themeColor":"#86b300"},"emojis":[]},"text":null,"cw":null,"visibility":"public","renoteCount":0,"repliesCount":0,"reactions":{},"emojis":[],"fileIds":[],"files":[],"replyId":null,"renoteId":"8ikcdkmw19","uri":"https://misskey.haibala.com/notes/8ikcfyi6np/activity","renote":{"id":"8ikcdkmw19","createdAt":"2021-02-22T16:11:47.000Z","userId":"7rqb0rlaxm","user":{"id":"7rqb0rlaxm","name":"直さらだ🔞","username":"nao_salad","host":"pawoo.net","avatarUrl":"https://nos3.arkjp.net/?url=https%3A%2F%2Fimg.pawoo.net%2Faccounts%2Favatars%2F000%2F186%2F181%2Foriginal%2F8a4cc93a0db29a81.png&thumbnail=1","avatarBlurhash":null,"avatarColor":null,"instance":{"name":"Pawoo","softwareName":null,"softwareVersion":null,"iconUrl":"https://pawoo.net/android-chrome-192x192.png","faviconUrl":"https://pawoo.net/favicon.ico","themeColor":"#282c37"},"emojis":[]},"text":null,"cw":null,"visibility":"public","renoteCount":2,"repliesCount":0,"reactions":{"❤":1},"emojis":[],"fileIds":["8ikcdm3p18"],"files":[{"id":"8ikcdm3p18","createdAt":"2021-02-22T16:11:48.901Z","name":"5b9c4b3fee5a3326.mp4","type":"video/mp4","md5":"064fb67a2b9add60b9c8b76673b8df02","size":0,"isSensitive":true,"blurhash":null,"properties":{},"url":"https://nos3.arkjp.net/?url=https%3A%2F%2Fimg.pawoo.net%2Fmedia_attachments%2Ffiles%2F034%2F139%2F866%2Foriginal%2F5b9c4b3fee5a3326.mp4","thumbnailUrl":"https://nos3.arkjp.net/?url=https%3A%2F%2Fimg.pawoo.net%2Fmedia_attachments%2Ffiles%2F034%2F139%2F866%2Foriginal%2F5b9c4b3fee5a3326.mp4&thumbnail=1","comment":null,"folderId":null,"folder":null,"userId":null,"user":null}],"replyId":null,"renoteId":null,"uri":"https://pawoo.net/users/nao_salad/statuses/105775779506713178","url":"https://pawoo.net/@nao_salad/105775779506713178"}}}}"""
        val s: StreamingEvent = response.decodeFromString(json)
        println(s)
        assertTrue(true)
    }

    @Test
    fun testDecodeNoteUpdated() {
        val json = """{"type":"noteUpdated","body":{"id":"8ikbe058jm","type":"reacted","body":{"reaction":"🙌","userId":"8g0o3hyk6r"}}}"""
        val s: StreamingEvent = Json.decodeFromString(json)
        println(s)
        assertTrue(true)
    }

    @Test
    fun testDecodeUnreacted() {
        val json = """{"type":"noteUpdated","body":{"id":"8ikbs5qq5z","type":"unreacted","body":{"reaction":"😆","userId":"7roinhytrr"}}}"""
        val s: StreamingEvent = Json.decodeFromString(json)
        println(s)
        assertTrue(true)
    }

    @Test
    fun testNoteDeleted() {
        val json  ="""{"type":"noteUpdated","body":{"id":"8ikcaxibzs","type":"deleted","body":{"deletedAt":"2021-02-22T16:09:47.568Z"}}}"""
        val s: StreamingEvent = Json.decodeFromString(json)
        println(s)
        assertTrue(true)
    }



    @Test
    fun testDecodePollVoteNotification() {
        val j = Json {
            ignoreUnknownKeys = true
        }
        val json = """{"type":"channel","body":{"id":"1","type":"notification","body":{"id":"8ikt10uewe","createdAt":"2021-02-22T23:57:54.950Z","type":"pollVote","isRead":false,"userId":"88wqchigvf","user":{"id":"88wqchigvf","name":"Lily","username":"Lily","host":null,"avatarUrl":"https://s3.arkjp.net/misskey/thumbnail-e833da1c-c1ca-47cc-b845-05000621341d.jpg","avatarBlurhash":"yPNI{oIn?|aK:Rxa#U00X-~DXR=zMx${'$'}+5PIAO;ozo}%goL^,xGRPi{nPoLjF-B%MwLn*sCR5R*aKnixts:a{aykCM{aKs;ofo3ozni","avatarColor":null,"emojis":[]},"note":{"id":"8iksycgnyx","createdAt":"2021-02-22T23:55:50.039Z","userId":"7roinhytrr","user":{"id":"7roinhytrr","name":"パン太","username":"Panta","host":null,"avatarUrl":"https://s3.arkjp.net/misskey/thumbnail-76a33500-270f-4acb-8b59-a033bb9e9593.jpg","avatarBlurhash":"yROpPl00AKk?9Gx]E3?^M|IVNfTJW=tRo}xuV@t7x]ofoL%2M{ENX9ozS2R*bcjFnhV[WXays.xtaeWXnhs.aeWVRkjYfkWAR*ofj?","avatarColor":null,"emojis":[]},"text":"test","cw":null,"visibility":"public","renoteCount":0,"repliesCount":0,"reactions":{},"emojis":[],"fileIds":[],"files":[],"replyId":null,"renoteId":null,"poll":{"multiple":false,"expiresAt":null,"choices":[{"text":"t1","votes":0,"isVoted":false},{"text":"t2","votes":1,"isVoted":false},{"text":"t3","votes":1,"isVoted":false}]}},"choice":1}}}"""
        val s: StreamingEvent =  j.decodeFromString(json)
        println(s)
        assertTrue(s is ChannelEvent && s.body is ChannelBody.Main.Notification)
    }
}

