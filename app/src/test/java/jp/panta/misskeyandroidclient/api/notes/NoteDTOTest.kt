package jp.panta.misskeyandroidclient.api.notes

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Test

class NoteDTOTest {

    @Test
    fun jsonDecodeTest() {
        val builder = Json {
            ignoreUnknownKeys = true
        }
        val jsonStr = """{"id":"8lp2oiif5t","createdAt":"2021-05-12T13:38:19.191Z","userId":"8bku1pzti4","user":{"id":"8bku1pzti4","name":"Thor 🇳🇴","username":"thor","host":"pl.thj.no","avatarUrl":"https://nos3.arkjp.net/?url=https%3A%2F%2Fpl.thj.no%2Fmedia%2Fbf68fb5dbe1ca0151a262f5f7c1a99f89c487e2da5c3f1fd6adbb1b871ca31be.jpeg&thumbnail=1","avatarBlurhash":"yNIYC0pyyYt-%#o}J8TK%#.7NytlS5tQIUV?nhxsjEWBV?x]bHbcozM|Rjt6WBkCbHR-R-xuxuIURjxZoeofofNGoyWCt7M{bHofR*","avatarColor":null,"instance":{"name":"Pleroma","softwareName":"pleroma","softwareVersion":"2.3.0-1-gb221d77a","iconUrl":"https://pl.thj.no/favicon.png","faviconUrl":"https://pl.thj.no/favicon.png","themeColor":null},"emojis":[],"onlineStatus":"unknown"},"text":null,"cw":null,"visibility":"public","renoteCount":0,"repliesCount":0,"reactions":{},"emojis":[],"fileIds":[],"files":[],"replyId":null,"renoteId":"8loy77x2cp","uri":"https://pl.thj.no/activities/498b3caf-e819-457b-b5fa-934bf5cd91a9","renote":{"id":"8loy77x2cp","createdAt":"2021-05-12T11:32:53.846Z","userId":"7y4q3ytt21","user":{"id":"7y4q3ytt21","name":"Solid Cанëк :sabakan: ","username":"solidsanek","host":"outerheaven.club","avatarUrl":"https://nos3.arkjp.net/?url=https%3A%2F%2Fouterheaven.club%2Fmedia%2F2b7478fa57151ffde725c6f41b18f215b1b4e6d4769805a89ff6244e0f9ceba3.blob&thumbnail=1","avatarBlurhash":"yiKwR.~Vk=Mxo#xtoz-;IBtQsVkBf7of%gRPxuxas:ofae_3xuM{M|RQW;Rjb^kCn%oyV@f+ofxuoyaeWqV@s:ofWWRjV@j[f6WBWB","avatarColor":null,"instance":{"name":"Outer Heaven","softwareName":"pleroma","softwareVersion":"2.3.0-1-gb221d77a","iconUrl":"https://outerheaven.club/favicon.png","faviconUrl":"https://outerheaven.club/favicon.png","themeColor":null},"emojis":[{"name":"sabakan","url":"https://outerheaven.club/emoji/mess/sabakan.png"}],"onlineStatus":"unknown"},"text":"Lunch time","cw":null,"visibility":"public","renoteCount":6,"repliesCount":1,"reactions":{"👍":7},"emojis":[],"fileIds":["8loy7diyco"],"files":[{"id":"8loy7diyco","createdAt":"2021-05-12T11:33:01.114Z","name":"ff0fb3a916107d7ae0adb95298a9cfd170a70b5265a7204f06bddfd40ab3f3b1.mp4","type":"video/mp4","md5":"2b00b47cd3dce6a0f1da04e6103db676","size":0,"isSensitive":false,"blurhash":null,"properties":{},"url":"https://nos3.arkjp.net/?url=https%3A%2F%2Fouterheaven.club%2Fmedia%2Fff0fb3a916107d7ae0adb95298a9cfd170a70b5265a7204f06bddfd40ab3f3b1.mp4","thumbnailUrl":"https://nos3.arkjp.net/?url=https%3A%2F%2Fouterheaven.club%2Fmedia%2Fff0fb3a916107d7ae0adb95298a9cfd170a70b5265a7204f06bddfd40ab3f3b1.mp4&thumbnail=1","comment":null,"folderId":null,"folder":null,"userId":null,"user":null}],"replyId":null,"renoteId":null,"uri":"https://outerheaven.club/objects/b6f14bd2-8ad1-4e4e-a5c4-ac318a3d61f0"}}"""
        val noteDTO: NoteDTO = builder.decodeFromString(jsonStr)
        Assert.assertNotNull(noteDTO)
    }
}