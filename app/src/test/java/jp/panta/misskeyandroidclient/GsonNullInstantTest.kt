package jp.panta.misskeyandroidclient

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.pantasystem.milktea.api.misskey.notes.NoteDTO
import org.junit.jupiter.api.Test


class GsonNullInstantTest {

    @Test
    fun testNullDateParse() {
        val decoder = Json {
            ignoreUnknownKeys = true
        }
        val json = """{"id":"8nt24ow8o5","createdAt":"2021-07-04T17:53:23.720Z","userId":"8lep49hc9a","user":{"id":"8lep49hc9a","name":"sh","username":"sfx_as","host":null,"avatarUrl":"https://s3.arkjp.net/misskey/thumbnail-b95de81d-c1cd-46ed-acd0-27ae4cd47182.png","avatarBlurhash":"yEF=K*Nu00E2yB~VIA009a~pogDj=xt+Iaspx=NG%2xt9G?Y-V4;Ip-nNFxb0L${'$'}*odR*-pMxxuEQxa%KxZE2NbnPNYRjRRxat5WCoz","avatarColor":null,"emojis":[],"onlineStatus":"unknown"},"text":null,"cw":null,"visibility":"public","viaMobile":true,"renoteCount":0,"repliesCount":0,"reactions":{},"emojis":[],"fileIds":[],"files":[],"replyId":null,"renoteId":null,"poll":{"multiple":false,"expiresAt":null,"choices":[{"text":" ","votes":1,"isVoted":false},{"text":"  ","votes":1,"isVoted":false},{"text":"   ","votes":1,"isVoted":false},{"text":"    ","votes":0,"isVoted":false},{"text":"     ","votes":0,"isVoted":false},{"text":"      ","votes":0,"isVoted":false},{"text":"       ","votes":0,"isVoted":false},{"text":"        ","votes":0,"isVoted":false},{"text":"         ","votes":1,"isVoted":false},{"text":"          ","votes":0,"isVoted":false}]}}"""
        val note: NoteDTO = decoder.decodeFromString(json)
        println(note)


    }
}