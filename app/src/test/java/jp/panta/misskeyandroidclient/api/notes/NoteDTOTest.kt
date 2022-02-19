package jp.panta.misskeyandroidclient.api.notes

import com.google.gson.Gson
import jp.panta.misskeyandroidclient.GsonFactory
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
        val jsonStr =
            """{"id":"8lp2oiif5t","createdAt":"2021-05-12T13:38:19.191Z","userId":"8bku1pzti4","user":{"id":"8bku1pzti4","name":"Thor 🇳🇴","username":"thor","host":"pl.thj.no","avatarUrl":"https://nos3.arkjp.net/?url=https%3A%2F%2Fpl.thj.no%2Fmedia%2Fbf68fb5dbe1ca0151a262f5f7c1a99f89c487e2da5c3f1fd6adbb1b871ca31be.jpeg&thumbnail=1","avatarBlurhash":"yNIYC0pyyYt-%#o}J8TK%#.7NytlS5tQIUV?nhxsjEWBV?x]bHbcozM|Rjt6WBkCbHR-R-xuxuIURjxZoeofofNGoyWCt7M{bHofR*","avatarColor":null,"instance":{"name":"Pleroma","softwareName":"pleroma","softwareVersion":"2.3.0-1-gb221d77a","iconUrl":"https://pl.thj.no/favicon.png","faviconUrl":"https://pl.thj.no/favicon.png","themeColor":null},"emojis":[],"onlineStatus":"unknown"},"text":null,"cw":null,"visibility":"public","renoteCount":0,"repliesCount":0,"reactions":{},"emojis":[],"fileIds":[],"files":[],"replyId":null,"renoteId":"8loy77x2cp","uri":"https://pl.thj.no/activities/498b3caf-e819-457b-b5fa-934bf5cd91a9","renote":{"id":"8loy77x2cp","createdAt":"2021-05-12T11:32:53.846Z","userId":"7y4q3ytt21","user":{"id":"7y4q3ytt21","name":"Solid Cанëк :sabakan: ","username":"solidsanek","host":"outerheaven.club","avatarUrl":"https://nos3.arkjp.net/?url=https%3A%2F%2Fouterheaven.club%2Fmedia%2F2b7478fa57151ffde725c6f41b18f215b1b4e6d4769805a89ff6244e0f9ceba3.blob&thumbnail=1","avatarBlurhash":"yiKwR.~Vk=Mxo#xtoz-;IBtQsVkBf7of%gRPxuxas:ofae_3xuM{M|RQW;Rjb^kCn%oyV@f+ofxuoyaeWqV@s:ofWWRjV@j[f6WBWB","avatarColor":null,"instance":{"name":"Outer Heaven","softwareName":"pleroma","softwareVersion":"2.3.0-1-gb221d77a","iconUrl":"https://outerheaven.club/favicon.png","faviconUrl":"https://outerheaven.club/favicon.png","themeColor":null},"emojis":[{"name":"sabakan","url":"https://outerheaven.club/emoji/mess/sabakan.png"}],"onlineStatus":"unknown"},"text":"Lunch time","cw":null,"visibility":"public","renoteCount":6,"repliesCount":1,"reactions":{"👍":7},"emojis":[],"fileIds":["8loy7diyco"],"files":[{"id":"8loy7diyco","createdAt":"2021-05-12T11:33:01.114Z","name":"ff0fb3a916107d7ae0adb95298a9cfd170a70b5265a7204f06bddfd40ab3f3b1.mp4","type":"video/mp4","md5":"2b00b47cd3dce6a0f1da04e6103db676","size":0,"isSensitive":false,"blurhash":null,"properties":{},"url":"https://nos3.arkjp.net/?url=https%3A%2F%2Fouterheaven.club%2Fmedia%2Fff0fb3a916107d7ae0adb95298a9cfd170a70b5265a7204f06bddfd40ab3f3b1.mp4","thumbnailUrl":"https://nos3.arkjp.net/?url=https%3A%2F%2Fouterheaven.club%2Fmedia%2Fff0fb3a916107d7ae0adb95298a9cfd170a70b5265a7204f06bddfd40ab3f3b1.mp4&thumbnail=1","comment":null,"folderId":null,"folder":null,"userId":null,"user":null}],"replyId":null,"renoteId":null,"uri":"https://outerheaven.club/objects/b6f14bd2-8ad1-4e4e-a5c4-ac318a3d61f0"}}"""
        val noteDTO: NoteDTO = builder.decodeFromString(jsonStr)
        Assert.assertNotNull(noteDTO)
    }

    @Test
    fun jsonDecodeTest2() {
        val jsonStr = "[\n" +
                "    {\n" +
                "        \"id\": \"8wx5r4lrqg\",\n" +
                "        \"createdAt\": \"2022-02-19T08:43:15.087Z\",\n" +
                "        \"userId\": \"8fu0rxwrdm\",\n" +
                "        \"user\": {\n" +
                "            \"id\": \"8fu0rxwrdm\",\n" +
                "            \"name\": \":_ze::_ro::_za::_su::_ki::wave:\",\n" +
                "            \"username\": \"zero_zaki_ghost\",\n" +
                "            \"host\": null,\n" +
                "            \"avatarUrl\": \"https://s3.arkjp.net/misskey/thumbnail-5d26d748-398f-4a68-a876-23e1852f22b1.jpg\",\n" +
                "            \"avatarBlurhash\": \"yGL|lz-p4njb-=s+aTRyog?Wa{D:WUWJ~lRjSIofN2j?jM^}M{Wmj@jLogIYI-Ri?GWBV|t7IZ?Zt6S0WVRqWD%3%Joyt6s:n.M{Iq\",\n" +
                "            \"avatarColor\": null,\n" +
                "            \"isCat\": true,\n" +
                "            \"emojis\": [\n" +
                "                {\n" +
                "                    \"name\": \"_ze\",\n" +
                "                    \"url\": \"https://s3.arkjp.net/misskey/webpublic-fcf3f781-4225-43d5-a5df-53b8936fad4d.png\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"_ro\",\n" +
                "                    \"url\": \"https://s3.arkjp.net/misskey/webpublic-9b45480c-fa64-4cea-be52-a71c4e67d15a.png\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"_za\",\n" +
                "                    \"url\": \"https://s3.arkjp.net/misskey/webpublic-d7fc9cef-60eb-4301-91e0-8c74bc41be2a.png\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"_su\",\n" +
                "                    \"url\": \"https://s3.arkjp.net/misskey/webpublic-91ec1ee3-23d2-42c7-890c-0ef9eed65620.png\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"_ki\",\n" +
                "                    \"url\": \"https://s3.arkjp.net/misskey/webpublic-ae353d86-489e-4178-b568-89b37ab0c16c.png\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"wave\",\n" +
                "                    \"url\": \"https://s3.arkjp.net/misskey/webpublic-127746e0-08fe-4e86-be39-1d71a9d35eeb.png\"\n" +
                "                }\n" +
                "            ],\n" +
                "            \"onlineStatus\": \"online\"\n" +
                "        },\n" +
                "        \"text\": \"いい感じのカレーが来た！\uD83C\uDF5B\",\n" +
                "        \"cw\": null,\n" +
                "        \"visibility\": \"public\",\n" +
                "        \"renoteCount\": 3,\n" +
                "        \"repliesCount\": 0,\n" +
                "        \"reactions\": {\n" +
                "            \"\uD83C\uDF5B\": 17,\n" +
                "            \"\uD83D\uDC4D\": 4,\n" +
                "            \"\uD83D\uDE0B\": 1,\n" +
                "            \":blobcatdroolreach@.:\": 1\n" +
                "        },\n" +
                "        \"emojis\": [\n" +
                "            {\n" +
                "                \"name\": \"blobcatdroolreach@.\",\n" +
                "                \"url\": \"https://s3.arkjp.net/misskey/webpublic-075124a1-5f71-4768-ba9f-8f5e1f5e485a.png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"fileIds\": [\n" +
                "            \"8wx5qr4in0\"\n" +
                "        ],\n" +
                "        \"files\": [\n" +
                "            {\n" +
                "                \"id\": \"8wx5qr4in0\",\n" +
                "                \"createdAt\": \"2022-02-19T08:42:57.618Z\",\n" +
                "                \"name\": \"DDB5D7BF-F9AE-4414-980C-22F850477AC4.jpeg\",\n" +
                "                \"type\": \"image/jpeg\",\n" +
                "                \"md5\": \"6d2a19a2fe519717dcb1bf786a527486\",\n" +
                "                \"size\": 3283785,\n" +
                "                \"isSensitive\": false,\n" +
                "                \"blurhash\": \"yZHB0R}sRQNGs:xtxasCV@ofxaxGRjWUe=bYt6Rjs:WXNat6s.ofV[a}oJbHfjoLf5aeRkaxofofbHayWV\",\n" +
                "                \"properties\": {\n" +
                "                    \"width\": 3024,\n" +
                "                    \"height\": 4032\n" +
                "                },\n" +
                "                \"url\": \"https://s3.arkjp.net/misskey/webpublic-51254b60-edd9-40c5-8c35-e8d6979be41a.jpg\",\n" +
                "                \"thumbnailUrl\": \"https://s3.arkjp.net/misskey/thumbnail-d3b5f1f8-93fa-43aa-9e4a-5ea80e259fe9.jpg\",\n" +
                "                \"comment\": null,\n" +
                "                \"folderId\": null,\n" +
                "                \"folder\": null,\n" +
                "                \"userId\": null,\n" +
                "                \"user\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"replyId\": null,\n" +
                "        \"renoteId\": null\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"8wx4sqjbld\",\n" +
                "        \"createdAt\": \"2022-02-19T08:16:30.551Z\",\n" +
                "        \"userId\": \"8fu0rxwrdm\",\n" +
                "        \"user\": {\n" +
                "            \"id\": \"8fu0rxwrdm\",\n" +
                "            \"name\": \":_ze::_ro::_za::_su::_ki::wave:\",\n" +
                "            \"username\": \"zero_zaki_ghost\",\n" +
                "            \"host\": null,\n" +
                "            \"avatarUrl\": \"https://s3.arkjp.net/misskey/thumbnail-5d26d748-398f-4a68-a876-23e1852f22b1.jpg\",\n" +
                "            \"avatarBlurhash\": \"yGL|lz-p4njb-=s+aTRyog?Wa{D:WUWJ~lRjSIofN2j?jM^}M{Wmj@jLogIYI-Ri?GWBV|t7IZ?Zt6S0WVRqWD%3%Joyt6s:n.M{Iq\",\n" +
                "            \"avatarColor\": null,\n" +
                "            \"isCat\": true,\n" +
                "            \"emojis\": [\n" +
                "                {\n" +
                "                    \"name\": \"_ze\",\n" +
                "                    \"url\": \"https://s3.arkjp.net/misskey/webpublic-fcf3f781-4225-43d5-a5df-53b8936fad4d.png\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"_ro\",\n" +
                "                    \"url\": \"https://s3.arkjp.net/misskey/webpublic-9b45480c-fa64-4cea-be52-a71c4e67d15a.png\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"_za\",\n" +
                "                    \"url\": \"https://s3.arkjp.net/misskey/webpublic-d7fc9cef-60eb-4301-91e0-8c74bc41be2a.png\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"_su\",\n" +
                "                    \"url\": \"https://s3.arkjp.net/misskey/webpublic-91ec1ee3-23d2-42c7-890c-0ef9eed65620.png\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"_ki\",\n" +
                "                    \"url\": \"https://s3.arkjp.net/misskey/webpublic-ae353d86-489e-4178-b568-89b37ab0c16c.png\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"wave\",\n" +
                "                    \"url\": \"https://s3.arkjp.net/misskey/webpublic-127746e0-08fe-4e86-be39-1d71a9d35eeb.png\"\n" +
                "                }\n" +
                "            ],\n" +
                "            \"onlineStatus\": \"online\"\n" +
                "        },\n" +
                "        \"text\": \"「毒親だけど距離置いてしばらくしたら和解した」って人がちょいちょいおるけど、そーゆう人尊敬する。俺には無理。\",\n" +
                "        \"cw\": null,\n" +
                "        \"visibility\": \"public\",\n" +
                "        \"renoteCount\": 7,\n" +
                "        \"repliesCount\": 0,\n" +
                "        \"reactions\": {\n" +
                "            \"❤\": 1,\n" +
                "            \"\uD83D\uDC4D\": 7\n" +
                "        },\n" +
                "        \"emojis\": [],\n" +
                "        \"fileIds\": [],\n" +
                "        \"files\": [],\n" +
                "        \"replyId\": null,\n" +
                "        \"renoteId\": null\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"8wx4j1g676\",\n" +
                "        \"createdAt\": \"2022-02-19T08:08:58.134Z\",\n" +
                "        \"userId\": \"7rkrg1wo1a\",\n" +
                "        \"user\": {\n" +
                "            \"id\": \"7rkrg1wo1a\",\n" +
                "            \"name\": \"村上さん\",\n" +
                "            \"username\": \"AureoleArk\",\n" +
                "            \"host\": null,\n" +
                "            \"avatarUrl\": \"https://s3.arkjp.net/misskey/thumbnail-80626a47-0654-4863-98e1-a7ecfb1c7131.jpg\",\n" +
                "            \"avatarBlurhash\": \"yHM8pYx]0n-TJCt89vLNEk+sadNK=vxYIUf+oAJWqT0%1RjDjI[%LS5NeRkNIt.i_NGbbv|SixujERkWYXTa#r;jY\",\n" +
                "            \"avatarColor\": null,\n" +
                "            \"isModerator\": true,\n" +
                "            \"emojis\": [],\n" +
                "            \"onlineStatus\": \"unknown\"\n" +
                "        },\n" +
                "        \"text\": null,\n" +
                "        \"cw\": null,\n" +
                "        \"visibility\": \"public\",\n" +
                "        \"renoteCount\": 4,\n" +
                "        \"repliesCount\": 0,\n" +
                "        \"reactions\": {\n" +
                "            \"\uD83D\uDC4D\": 1,\n" +
                "            \"\uD83D\uDE07\": 4,\n" +
                "            \"\uD83E\uDD74\": 4,\n" +
                "            \":bap@.:\": 2\n" +
                "        },\n" +
                "        \"emojis\": [\n" +
                "            {\n" +
                "                \"name\": \"bap@.\",\n" +
                "                \"url\": \"https://s3.arkjp.net/misskey/1a8e89a6-10fd-4315-85ad-491bd1144058.gif\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"fileIds\": [\n" +
                "            \"8wx4j0frcs\"\n" +
                "        ],\n" +
                "        \"files\": [\n" +
                "            {\n" +
                "                \"id\": \"8wx4j0frcs\",\n" +
                "                \"createdAt\": \"2022-02-19T08:08:56.823Z\",\n" +
                "                \"name\": \"2022-02-19 17-08-56 1.png\",\n" +
                "                \"type\": \"image/png\",\n" +
                "                \"md5\": \"f04366a251295387280bc0788293f0b4\",\n" +
                "                \"size\": 384761,\n" +
                "                \"isSensitive\": false,\n" +
                "                \"blurhash\": \"yLAB6UaI9GnMnes8rByGjFRjjEjEj_bJXoL}oz%2kEbeX9XU^%WAE3j]kWW=nNxvocNKodt5oIngD*ofxtaejEslbc\",\n" +
                "                \"properties\": {\n" +
                "                    \"width\": 615,\n" +
                "                    \"height\": 737\n" +
                "                },\n" +
                "                \"url\": \"https://s3.arkjp.net/misskey/webpublic-574ad5eb-66d6-42b7-b59f-e99a744dc140.png\",\n" +
                "                \"thumbnailUrl\": \"https://s3.arkjp.net/misskey/thumbnail-b8ec2fd3-3b55-4c0e-bb2c-2a38e966a190.jpg\",\n" +
                "                \"comment\": null,\n" +
                "                \"folderId\": \"7v9lb3aif9\",\n" +
                "                \"folder\": null,\n" +
                "                \"userId\": null,\n" +
                "                \"user\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"replyId\": null,\n" +
                "        \"renoteId\": null\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"8wx4at7wy5\",\n" +
                "        \"createdAt\": \"2022-02-19T08:02:34.220Z\",\n" +
                "        \"userId\": \"8g0j5lv00n\",\n" +
                "        \"user\": {\n" +
                "            \"id\": \"8g0j5lv00n\",\n" +
                "            \"name\": \"t_w 79.9kg\",\n" +
                "            \"username\": \"t_w\",\n" +
                "            \"host\": null,\n" +
                "            \"avatarUrl\": \"https://s3.arkjp.net/misskey/thumbnail-bfc014aa-76d8-45eb-87a5-8db3e5c1baa1.png\",\n" +
                "            \"avatarBlurhash\": \"yUM~qO9t7\$+xaD~SO9ZxuxaVtV[%MoyWBbbIVIUsp%LxtofNGX7ozRjI:W=oc-pxtNGf+t7WBxas.oybFRjxtoKWCj]WAoLWV\",\n" +
                "            \"avatarColor\": null,\n" +
                "            \"emojis\": [],\n" +
                "            \"onlineStatus\": \"online\"\n" +
                "        },\n" +
                "        \"text\": \"Rustで一家離散させて遊んでる\",\n" +
                "        \"cw\": null,\n" +
                "        \"visibility\": \"public\",\n" +
                "        \"renoteCount\": 7,\n" +
                "        \"repliesCount\": 2,\n" +
                "        \"reactions\": {\n" +
                "            \"\uD83C\uDF89\": 1,\n" +
                "            \"\uD83D\uDC4D\": 4,\n" +
                "            \":shikei@.:\": 4\n" +
                "        },\n" +
                "        \"emojis\": [\n" +
                "            {\n" +
                "                \"name\": \"shikei@.\",\n" +
                "                \"url\": \"https://s3.arkjp.net/misskey/webpublic-01d065e5-a99e-455b-b449-f96b4a79fb52.png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"fileIds\": [\n" +
                "            \"8wx4altv55\"\n" +
                "        ],\n" +
                "        \"files\": [\n" +
                "            {\n" +
                "                \"id\": \"8wx4altv55\",\n" +
                "                \"createdAt\": \"2022-02-19T08:02:24.643Z\",\n" +
                "                \"name\": \"2022-02-19 17-02-24 1.png\",\n" +
                "                \"type\": \"image/png\",\n" +
                "                \"md5\": \"ce5b2ed518a903daae3ff57736c2ba91\",\n" +
                "                \"size\": 37629,\n" +
                "                \"isSensitive\": false,\n" +
                "                \"blurhash\": \"y05OQl_E7IXNfI=I;00\$*s;Vu%1j=j?~qNYI.NFM{ayV[%MM|t8t7R*R*ba00xvxbs;s:RkRj_3tPR%t6R%RjR+\",\n" +
                "                \"properties\": {\n" +
                "                    \"width\": 462,\n" +
                "                    \"height\": 542\n" +
                "                },\n" +
                "                \"url\": \"https://s3.arkjp.net/misskey/webpublic-50ae16b4-b466-4b67-ad52-5fb712dcae17.png\",\n" +
                "                \"thumbnailUrl\": \"https://s3.arkjp.net/misskey/thumbnail-129337e4-7233-4c5e-8a0c-fb425208b3a5.jpg\",\n" +
                "                \"comment\": null,\n" +
                "                \"folderId\": null,\n" +
                "                \"folder\": null,\n" +
                "                \"userId\": null,\n" +
                "                \"user\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"replyId\": null,\n" +
                "        \"renoteId\": null\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"8wx3kz489a\",\n" +
                "        \"createdAt\": \"2022-02-19T07:42:28.808Z\",\n" +
                "        \"userId\": \"7rkrg1wo1a\",\n" +
                "        \"user\": {\n" +
                "            \"id\": \"7rkrg1wo1a\",\n" +
                "            \"name\": \"村上さん\",\n" +
                "            \"username\": \"AureoleArk\",\n" +
                "            \"host\": null,\n" +
                "            \"avatarUrl\": \"https://s3.arkjp.net/misskey/thumbnail-80626a47-0654-4863-98e1-a7ecfb1c7131.jpg\",\n" +
                "            \"avatarBlurhash\": \"yHM8pYx]0n-TJCt89vLNEk+sadNK=vxYIUf+oEQAJWqT0%1RjDjI[%LS5NeRkNIt.i_NGbbv|SixujERkWYXTa#r;jY\",\n" +
                "            \"avatarColor\": null,\n" +
                "            \"isModerator\": true,\n" +
                "            \"emojis\": [],\n" +
                "            \"onlineStatus\": \"unknown\"\n" +
                "        },\n" +
                "        \"text\": \"Misskeyの完全なSVGロゴなかったので作った\\n（公式のSVGはPNGがbase64に変換されたものが埋め込まれている）\\n\\nhttps://s3.arkjp.net/misskey/3219e602-a0fe-42c7-aa35-f39ddcf90fc2\",\n" +
                "        \"cw\": null,\n" +
                "        \"visibility\": \"public\",\n" +
                "        \"renoteCount\": 10,\n" +
                "        \"repliesCount\": 0,\n" +
                "        \"reactions\": {\n" +
                "            \"\uD83C\uDF89\": 1,\n" +
                "            \"\uD83D\uDE0A\": 1,\n" +
                "            \"\uD83E\uDD70\": 1,\n" +
                "            \":igyo@.:\": 1,\n" +
                "            \":misskey@.:\": 4,\n" +
                "            \":nacho_hi@.:\": 1,\n" +
                "            \":arigatofes@.:\": 1,\n" +
                "            \":kami@nca10.net:\": 1,\n" +
                "            \":misskey@fedibird.com:\": 1,\n" +
                "            \":iihanashi@umaskey.net:\": 1,\n" +
                "            \":blob_hearteyes@sushi.ski:\": 1,\n" +
                "            \":igyou@misky.rikunagiweb.jp:\": 1\n" +
                "        },\n" +
                "        \"emojis\": [\n" +
                "            {\n" +
                "                \"name\": \"igyo@.\",\n" +
                "                \"url\": \"https://s3.arkjp.net/misskey/webpublic-d50d9d65-8413-4236-9762-393e4f3585ce.png\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"misskey@.\",\n" +
                "                \"url\": \"https://s3.arkjp.net/misskey/webpublic-f3ca12a3-0254-4326-aac8-b79915fc34d6.png\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"nacho_hi@.\",\n" +
                "                \"url\": \"https://s3.arkjp.net/misskey/webpublic-105da421-b3af-48fa-af6f-d616f9510823.png\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"arigatofes@.\",\n" +
                "                \"url\": \"https://s3.arkjp.net/misskey/webpublic-ddbfee9d-91ab-419a-9247-64dd90b62fac.png\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"kami@nca10.net\",\n" +
                "                \"url\": \"https://misskey.io/proxy/image.png?url=https%3A%2F%2Fs3.nca10.net%2Fmisskey%2Fwebpublic-1e162464-dbd5-48ce-bd01-39d12e5082ed.png\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"hosii@nca10.net\",\n" +
                "                \"url\": \"https://misskey.io/proxy/image.png?url=https%3A%2F%2Fs3.nca10.net%2Fmisskey%2Fwebpublic-ef879915-4a38-43f1-8d64-84c763bbbeae.png\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"misskey@fedibird.com\",\n" +
                "                \"url\": \"https://misskey.io/proxy/image.png?url=https%3A%2F%2Fs3.fedibird.com%2Fcustom_emojis%2Fimages%2F000%2F008%2F417%2Foriginal%2Fb081a4cecfbf0750.png\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"iihanashi@umaskey.net\",\n" +
                "                \"url\": \"https://misskey.io/proxy/image.png?url=https%3A%2F%2Fumaskey.net%2Ffiles%2F9249dae6-22f8-4536-9723-3ef10a1e6b66\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"blob_hearteyes@sushi.ski\",\n" +
                "                \"url\": \"https://misskey.io/proxy/image.png?url=https%3A%2F%2Fmedia.sushi.ski%2Ffiles%2Fd013b1a9-8c40-48d3-827f-53422814419d.gif\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"igyou@misky.rikunagiweb.jp\",\n" +
                "                \"url\": \"https://misskey.io/proxy/image.png?url=https%3A%2F%2Fmedia-misky.rikunagiweb.jp%2Fmedia%2Fwebpublic-9244077e-df15-4c9f-a02f-5ad4d25fe3bd.png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"fileIds\": [\n" +
                "            \"8wx3hl69v7\"\n" +
                "        ],\n" +
                "        \"files\": [\n" +
                "            {\n" +
                "                \"id\": \"8wx3hl69v7\",\n" +
                "                \"createdAt\": \"2022-02-19T07:39:50.769Z\",\n" +
                "                \"name\": \"mi.svg\",\n" +
                "                \"type\": \"image/svg+xml\",\n" +
                "                \"md5\": \"2167b6ce26cd2c1d78530562f0a4188a\",\n" +
                "                \"size\": 5837,\n" +
                "                \"isSensitive\": false,\n" +
                "                \"blurhash\": \"yHE;,e%r.g%sy8MkM#yAWCWYoJj[V_af8,M*th%bkBM%RTtNV^t5oej@j@WCRBV]oxf7kAj@ayagayovovkAazk9RSoxRSRTagkAow\",\n" +
                "                \"properties\": {\n" +
                "                    \"width\": 1345.9,\n" +
                "                    \"height\": 985.05\n" +
                "                },\n" +
                "                \"url\": \"https://s3.arkjp.net/misskey/webpublic-597033c8-49aa-4adb-bbc6-d025e85c2df4.png\",\n" +
                "                \"thumbnailUrl\": \"https://s3.arkjp.net/misskey/thumbnail-b34d0613-c969-40f7-a529-39725519e736.png\",\n" +
                "                \"comment\": null,\n" +
                "                \"folderId\": \"7v9lb3aif9\",\n" +
                "                \"folder\": null,\n" +
                "                \"userId\": null,\n" +
                "                \"user\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"replyId\": null,\n" +
                "        \"renoteId\": null\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"8wx3dgiiss\",\n" +
                "        \"createdAt\": \"2022-02-19T07:36:38.106Z\",\n" +
                "        \"userId\": \"8fu0rxwrdm\",\n" +
                "        \"user\": {\n" +
                "            \"id\": \"8fu0rxwrdm\",\n" +
                "            \"name\": \":_ze::_ro::_za::_su::_ki::wave:\",\n" +
                "            \"username\": \"zero_zaki_ghost\",\n" +
                "            \"host\": null,\n" +
                "            \"avatarUrl\": \"https://s3.arkjp.net/misskey/thumbnail-5d26d748-398f-4a68-a876-23e1852f22b1.jpg\",\n" +
                "            \"avatarBlurhash\": \"yGL|lz-p4njb-=s+aTRyog?Wa{D:WUWJ~lRjSIofN2j?jM^}M{Wmj@jLogIYI-Ri?GWBV|t7IZ?Zt6S0WVRqWD%3%Joyt6s:n.M{Iq\",\n" +
                "            \"avatarColor\": null,\n" +
                "            \"isCat\": true,\n" +
                "            \"emojis\": [\n" +
                "                {\n" +
                "                    \"name\": \"_ze\",\n" +
                "                    \"url\": \"https://s3.arkjp.net/misskey/webpublic-fcf3f781-4225-43d5-a5df-53b8936fad4d.png\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"_ro\",\n" +
                "                    \"url\": \"https://s3.arkjp.net/misskey/webpublic-9b45480c-fa64-4cea-be52-a71c4e67d15a.png\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"_za\",\n" +
                "                    \"url\": \"https://s3.arkjp.net/misskey/webpublic-d7fc9cef-60eb-4301-91e0-8c74bc41be2a.png\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"_su\",\n" +
                "                    \"url\": \"https://s3.arkjp.net/misskey/webpublic-91ec1ee3-23d2-42c7-890c-0ef9eed65620.png\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"_ki\",\n" +
                "                    \"url\": \"https://s3.arkjp.net/misskey/webpublic-ae353d86-489e-4178-b568-89b37ab0c16c.png\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"name\": \"wave\",\n" +
                "                    \"url\": \"https://s3.arkjp.net/misskey/webpublic-127746e0-08fe-4e86-be39-1d71a9d35eeb.png\"\n" +
                "                }\n" +
                "            ],\n" +
                "            \"onlineStatus\": \"online\"\n" +
                "        },\n" +
                "        \"text\": \"それでは〜？ぺーい！\uD83C\uDF7B\",\n" +
                "        \"cw\": null,\n" +
                "        \"visibility\": \"public\",\n" +
                "        \"renoteCount\": 1,\n" +
                "        \"repliesCount\": 1,\n" +
                "        \"reactions\": {\n" +
                "            \"\uD83C\uDF7B\": 14,\n" +
                "            \"\uD83D\uDC4D\": 3\n" +
                "        },\n" +
                "        \"emojis\": [],\n" +
                "        \"fileIds\": [\n" +
                "            \"8wx3dbuk47\"\n" +
                "        ],\n" +
                "        \"files\": [\n" +
                "            {\n" +
                "                \"id\": \"8wx3dbuk47\",\n" +
                "                \"createdAt\": \"2022-02-19T07:36:32.060Z\",\n" +
                "                \"name\": \"D1D2514D-8FF8-4524-ABB4-2CF8235E60A7.jpeg\",\n" +
                "                \"type\": \"image/jpeg\",\n" +
                "                \"md5\": \"ddefed5454d0288f234799d700f69d9f\",\n" +
                "                \"size\": 3300034,\n" +
                "                \"isSensitive\": false,\n" +
                "                \"blurhash\": \"y9I4CDD+?a57-U-VIp~BE3Sj%1M|IqR*IWE2WBRjWBRkbGNdt7%1t6ofs:t6%1R+I;xss:R+X7xDWBR+NGWBNIofoft6f*xss:s:WU\",\n" +
                "                \"properties\": {\n" +
                "                    \"width\": 3024,\n" +
                "                    \"height\": 4032\n" +
                "                },\n" +
                "                \"url\": \"https://s3.arkjp.net/misskey/webpublic-71a17c67-a2ea-41e3-9102-4c2040528864.jpg\",\n" +
                "                \"thumbnailUrl\": \"https://s3.arkjp.net/misskey/thumbnail-8d5611f8-36d2-402c-8cc6-7e10f8e9d3f6.jpg\",\n" +
                "                \"comment\": null,\n" +
                "                \"folderId\": null,\n" +
                "                \"folder\": null,\n" +
                "                \"userId\": null,\n" +
                "                \"user\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"replyId\": null,\n" +
                "        \"renoteId\": null\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"8wwwqjdrhi\",\n" +
                "        \"createdAt\": \"2022-02-19T04:30:51.039Z\",\n" +
                "        \"userId\": \"7rkrarq81i\",\n" +
                "        \"user\": {\n" +
                "            \"id\": \"7rkrarq81i\",\n" +
                "            \"name\": \"しゅいろ\",\n" +
                "            \"username\": \"syuilo\",\n" +
                "            \"host\": null,\n" +
                "            \"avatarUrl\": \"https://s3.arkjp.net/misskey/thumbnail-c7721442-e698-4635-a662-57d78856bbc0.jpg\",\n" +
                "            \"avatarBlurhash\": \"yFF5Kq0L00?a^*IBNG01^j-pV@D*o|xt58WB}@9at7s.Ip~AWB57%Laes:xaOEoLnis:ofIpoJr?NHtRV@oLoeNHNI%1M{kCWCjuxZ\",\n" +
                "            \"avatarColor\": null,\n" +
                "            \"isModerator\": true,\n" +
                "            \"isCat\": true,\n" +
                "            \"emojis\": [],\n" +
                "            \"onlineStatus\": \"online\"\n" +
                "        },\n" +
                "        \"text\": \"コロニャの影響でコンビニ閉まってた:sonnakotoarunda:\",\n" +
                "        \"cw\": null,\n" +
                "        \"visibility\": \"public\",\n" +
                "        \"renoteCount\": 5,\n" +
                "        \"repliesCount\": 0,\n" +
                "        \"reactions\": {\n" +
                "            \"⭐\": 4,\n" +
                "            \"\uD83D\uDC4D\": 6,\n" +
                "            \"\uD83D\uDE25\": 4,\n" +
                "            \"\uD83D\uDE2E\": 2,\n" +
                "            \"\uD83E\uDD14\": 1,\n" +
                "            \":murishite@.:\": 1,\n" +
                "            \":maanantekoto@.:\": 1,\n" +
                "            \":omae_ga_tsukure@.:\": 1,\n" +
                "            \":sonnakotoarunda@.:\": 5,\n" +
                "            \":nacho_cry@nya.one:\": 1,\n" +
                "            \":ablobcatblinkhyper@.:\": 1,\n" +
                "            \":ablobcatcryingcute@.:\": 2,\n" +
                "            \":crying_cat@nca10.net:\": 1,\n" +
                "            \":blob_sleepy@sushi.ski:\": 1,\n" +
                "            \":blobsleepless@k.lapy.link:\": 1,\n" +
                "            \":cyber_hacking@mk.f72u.net:\": 1,\n" +
                "            \":blobcatcry@misskey.m544.net:\": 1,\n" +
                "            \":sonnakotoarunda@friendsyu.me:\": 1,\n" +
                "            \":sonnakotoarunda@mk.lei202.com:\": 1\n" +
                "        },\n" +
                "        \"emojis\": [\n" +
                "            {\n" +
                "                \"name\": \"sonnakotoarunda\",\n" +
                "                \"url\": \"https://s3.arkjp.net/misskey/webpublic-52ee9700-7114-4690-88cf-8633b2ad962a.png\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"murishite@.\",\n" +
                "                \"url\": \"https://s3.arkjp.net/misskey/webpublic-1f9050ad-bdc8-4c86-8c07-68f70f55f887.png\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"maanantekoto@.\",\n" +
                "                \"url\": \"https://s3.arkjp.net/misskey/webpublic-15ec5fe4-42ec-41f8-8cd8-fdfafefea93c.png\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"omae_ga_tsukure@.\",\n" +
                "                \"url\": \"https://s3.arkjp.net/misskey/webpublic-0c0c7db8-359b-4caf-ae95-5e9a47312f42.png\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"sonnakotoarunda@.\",\n" +
                "                \"url\": \"https://s3.arkjp.net/misskey/webpublic-52ee9700-7114-4690-88cf-8633b2ad962a.png\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"nacho_cry@nya.one\",\n" +
                "                \"url\": \"https://misskey.io/proxy/image.png?url=https%3A%2F%2Ffile.nya.one%2Fmisskey%2Fwebpublic-494cfc89-a3c7-46a5-95d8-fb362bacd3ba.png\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"ablobcatblinkhyper@.\",\n" +
                "                \"url\": \"https://s3.arkjp.net/misskey/c1d1be32-f3c3-4c60-9509-cb690b7935a8\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"ablobcatcryingcute@.\",\n" +
                "                \"url\": \"https://s3.arkjp.net/misskey/b90b23ac-432d-4bd2-9e58-8ee6980f5595.apng\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"crying_cat@nca10.net\",\n" +
                "                \"url\": \"https://misskey.io/proxy/image.png?url=https%3A%2F%2Fs3.nca10.net%2Fmisskey%2Fc5f30fa7-2459-47eb-a9ce-060bf6c617d6.apng\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"blob_sleepy@sushi.ski\",\n" +
                "                \"url\": \"https://misskey.io/proxy/image.png?url=https%3A%2F%2Fmedia.sushi.ski%2Ffiles%2F3f6f4034-f527-4e3a-bbb6-406dbcefb9b1.gif\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"blobsleepless@k.lapy.link\",\n" +
                "                \"url\": \"https://misskey.io/proxy/image.png?url=https%3A%2F%2Fmisskeylapy.s3.amazonaws.com%2Fnull%2F6fe193f7-a30e-48ad-abba-04f7e92c597e.png\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"cyber_hacking@mk.f72u.net\",\n" +
                "                \"url\": \"https://misskey.io/proxy/image.png?url=https%3A%2F%2Fmk.f72u.net%2Fmedia%2Fmisskey%2F88c357e5-0c30-43c9-9cf5-adb009e0a916.gif\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"blobcatcry@misskey.m544.net\",\n" +
                "                \"url\": \"https://misskey.io/proxy/image.png?url=https%3A%2F%2Fmisskey-drive2.m544.net%2Fm544%2Fzcj6iwsig8iw4c9r29cegarn.png\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"sonnakotoarunda@friendsyu.me\",\n" +
                "                \"url\": \"https://misskey.io/proxy/image.png?url=https%3A%2F%2Ffriendsyu.me%2Ffiles%2F602edca04d88a20c3708cd38%2F602edca04d88a20c3708cd38.png%3Fweb\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"sonnakotoarunda@mk.lei202.com\",\n" +
                "                \"url\": \"https://misskey.io/proxy/image.png?url=https%3A%2F%2Fmk.lei202.com%2Ffiles%2F6136515418e61a041d63583e%2F6136515418e61a041d63583e.png%3Fweb\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"fileIds\": [\n" +
                "            \"8wwwpv6ex6\"\n" +
                "        ],\n" +
                "        \"files\": [\n" +
                "            {\n" +
                "                \"id\": \"8wwwpv6ex6\",\n" +
                "                \"createdAt\": \"2022-02-19T04:30:19.670Z\",\n" +
                "                \"name\": \"D556EB85-FF92-4592-B5F4-909F221C98D4.jpeg\",\n" +
                "                \"type\": \"image/jpeg\",\n" +
                "                \"md5\": \"f20b1343b469a36ac0e7c5736926a406\",\n" +
                "                \"size\": 4090988,\n" +
                "                \"isSensitive\": false,\n" +
                "                \"blurhash\": \"yRG+UNM{ads:E1ofM|_Nt5adIUjYRjRj?cIVM_aeM|Rjay.9M{V?M{WAocRj?bayRjaeoJRkjZ?bj[ofj?Rjofax%MWAfQayj@oeRj\",\n" +
                "                \"properties\": {\n" +
                "                    \"width\": 4032,\n" +
                "                    \"height\": 3024\n" +
                "                },\n" +
                "                \"url\": \"https://s3.arkjp.net/misskey/webpublic-04603751-9ed4-44e9-81a8-462f31ce6cde.jpg\",\n" +
                "                \"thumbnailUrl\": \"https://s3.arkjp.net/misskey/thumbnail-9ea18611-136c-4443-b782-293a627c7a97.jpg\",\n" +
                "                \"comment\": null,\n" +
                "                \"folderId\": null,\n" +
                "                \"folder\": null,\n" +
                "                \"userId\": null,\n" +
                "                \"user\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"replyId\": null,\n" +
                "        \"renoteId\": null\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"8wwscscb12\",\n" +
                "        \"createdAt\": \"2022-02-19T02:28:11.003Z\",\n" +
                "        \"userId\": \"86p1tmjaav\",\n" +
                "        \"user\": {\n" +
                "            \"id\": \"86p1tmjaav\",\n" +
                "            \"name\": \"みれい (1,650km・5.9E+16t)\",\n" +
                "            \"username\": \"Mi\",\n" +
                "            \"host\": null,\n" +
                "            \"avatarUrl\": \"https://s3.arkjp.net/misskey/thumbnail-6c009d3c-e7cd-4f55-96a4-2f1b99670f4c.jpg\",\n" +
                "            \"avatarBlurhash\": \"yiKLUEof*0kW.8t7RjI;ay-pofIUWBoex^ay8ayM{bIadWBjZofjsV?bHbHWBtRxvfPoKbHWBayoft7f6RjbHt7ayjt\",\n" +
                "            \"avatarColor\": null,\n" +
                "            \"emojis\": [],\n" +
                "            \"onlineStatus\": \"online\"\n" +
                "        },\n" +
                "        \"text\": \"1,650kmのみれいさん Vs. 日本列島\",\n" +
                "        \"cw\": null,\n" +
                "        \"visibility\": \"public\",\n" +
                "        \"renoteCount\": 8,\n" +
                "        \"repliesCount\": 3,\n" +
                "        \"reactions\": {\n" +
                "            \"\uD83D\uDC4D\": 4,\n" +
                "            \"\uD83E\uDD14\": 1,\n" +
                "            \":cyber_hacking@.:\": 2,\n" +
                "            \":ablobdundundun@.:\": 2,\n" +
                "            \":confusedparrot@fedibird.com:\": 1,\n" +
                "            \":octothink@misky.rikunagiweb.jp:\": 1\n" +
                "        },\n" +
                "        \"emojis\": [\n" +
                "            {\n" +
                "                \"name\": \"cyber_hacking@.\",\n" +
                "                \"url\": \"https://s3.arkjp.net/misskey/f4ace218-272f-49e7-8813-66f47db9efba\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"ablobdundundun@.\",\n" +
                "                \"url\": \"https://s3.arkjp.net/misskey/c62c5cef-b9c0-4d8f-b0ce-2e2a358abe9a.gif\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"confusedparrot@fedibird.com\",\n" +
                "                \"url\": \"https://misskey.io/proxy/image.png?url=https%3A%2F%2Fs3.fedibird.com%2Fcustom_emojis%2Fimages%2F000%2F049%2F092%2Foriginal%2Fdbdc78a4a85a4b50.gif\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"octothink@misky.rikunagiweb.jp\",\n" +
                "                \"url\": \"https://misskey.io/proxy/image.png?url=https%3A%2F%2Fmedia-misky.rikunagiweb.jp%2Fmedia%2Fwebpublic-9511a7bf-1cbd-46c5-a0f5-481f8617d1e1.png\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"fileIds\": [\n" +
                "            \"8wwsccpzeq\"\n" +
                "        ],\n" +
                "        \"files\": [\n" +
                "            {\n" +
                "                \"id\": \"8wwsccpzeq\",\n" +
                "                \"createdAt\": \"2022-02-19T02:27:50.759Z\",\n" +
                "                \"name\": \"みれいさん.png\",\n" +
                "                \"type\": \"image/png\",\n" +
                "                \"md5\": \"cfd22a363a7829a356f860499e636081\",\n" +
                "                \"size\": 2034258,\n" +
                "                \"isSensitive\": true,\n" +
                "                \"blurhash\": \"y97K|^_LM{IoofWCaz-.xsoet7s:RjRjR%axs:oNoftQoxtQt7RkIVNFt7t7IVayxukCNGWBj[ayj?jZWBoft7f8xtofR%bFoyf8a#\",\n" +
                "                \"properties\": {\n" +
                "                    \"width\": 1658,\n" +
                "                    \"height\": 981\n" +
                "                },\n" +
                "                \"url\": \"https://s3.arkjp.net/misskey/webpublic-fec77b5e-f322-47b8-b063-e1830c835f9f.png\",\n" +
                "                \"thumbnailUrl\": \"https://s3.arkjp.net/misskey/thumbnail-0c309337-dcb1-44a5-bd78-8e4b625adacb.png\",\n" +
                "                \"comment\": null,\n" +
                "                \"folderId\": null,\n" +
                "                \"folder\": null,\n" +
                "                \"userId\": null,\n" +
                "                \"user\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"replyId\": null,\n" +
                "        \"renoteId\": null\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"8ww3dhb07s\",\n" +
                "        \"createdAt\": \"2022-02-18T14:48:52.956Z\",\n" +
                "        \"userId\": \"7rkrarq81i\",\n" +
                "        \"user\": {\n" +
                "            \"id\": \"7rkrarq81i\",\n" +
                "            \"name\": \"しゅいろ\",\n" +
                "            \"username\": \"syuilo\",\n" +
                "            \"host\": null,\n" +
                "            \"avatarUrl\": \"https://s3.arkjp.net/misskey/thumbnail-c7721442-e698-4635-a662-57d78856bbc0.jpg\",\n" +
                "            \"avatarBlurhash\": \"yFF5Kq0L00?a^*IBNG01^j-pV@D*o|xt58WB}@9at7s.Ip~AWB57%Laes:xaOEoLnis:ofIpoJr?NHtRV@oLoeNHNI%1M{kCWCjuxZ\",\n" +
                "            \"avatarColor\": null,\n" +
                "            \"isModerator\": true,\n" +
                "            \"isCat\": true,\n" +
                "            \"emojis\": [],\n" +
                "            \"onlineStatus\": \"online\"\n" +
                "        },\n" +
                "        \"text\": \"テストが3時間経っても終わらにゃいの:ijo:\",\n" +
                "        \"cw\": null,\n" +
                "        \"visibility\": \"public\",\n" +
                "        \"renoteCount\": 3,\n" +
                "        \"repliesCount\": 0,\n" +
                "        \"reactions\": {\n" +
                "            \"⭐\": 6,\n" +
                "            \"\uD83D\uDC4D\": 5,\n" +
                "            \":ijo@.:\": 3,\n" +
                "            \":yabaiwayo@.:\": 1,\n" +
                "            \":senko_stop@.:\": 1,\n" +
                "            \":ijo@sushi.ski:\": 1,\n" +
                "            \":issue@msk.minetaro12.com:\": 1\n" +
                "        },\n" +
                "        \"emojis\": [\n" +
                "            {\n" +
                "                \"name\": \"ijo\",\n" +
                "                \"url\": \"https://s3.arkjp.net/misskey/webpublic-085c1e56-1157-4c7a-9b93-b43be4f7d7ef.png\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"ijo@.\",\n" +
                "                \"url\": \"https://s3.arkjp.net/misskey/webpublic-085c1e56-1157-4c7a-9b93-b43be4f7d7ef.png\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"yabaiwayo@.\",\n" +
                "                \"url\": \"https://s3.arkjp.net/misskey/webpublic-4c3e962c-2b2a-48fa-845b-a0822eef733d.png\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"senko_stop@.\",\n" +
                "                \"url\": \"https://s3.arkjp.net/misskey/webpublic-d600ad33-561c-4f39-a103-9772529395a9.png\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"ijo@sushi.ski\",\n" +
                "                \"url\": \"https://misskey.io/proxy/image.png?url=https%3A%2F%2Fmedia.sushi.ski%2Ffiles%2Fwebpublic-3678f3f9-727d-4d07-85b0-b9d46d9cd88f.png\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"issue@msk.minetaro12.com\",\n" +
                "                \"url\": \"https://misskey.io/proxy/image.png?url=https%3A%2F%2Fmsk.minetaro12.com%2Ffiles%2Fwebpublic-ef33fbde-c155-44a4-a529-ff4515d01873\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"fileIds\": [\n" +
                "            \"8ww3cxdn8i\"\n" +
                "        ],\n" +
                "        \"files\": [\n" +
                "            {\n" +
                "                \"id\": \"8ww3cxdn8i\",\n" +
                "                \"createdAt\": \"2022-02-18T14:48:27.131Z\",\n" +
                "                \"name\": \"スクリーンショット 2022-02-18 23.47.40.png\",\n" +
                "                \"type\": \"image/png\",\n" +
                "                \"md5\": \"c6d0fec428c2a5cc0f8640a3b89b9660\",\n" +
                "                \"size\": 264526,\n" +
                "                \"isSensitive\": false,\n" +
                "                \"blurhash\": \"y01.]T%NRiadV?ofofozt7%gt8xuWAM{ITt8x]j]WBWBfjV[bJM{ogWBWCt7M{t8RkRPofayj]xvaxt7ozWAaxRj%Noet7jYaef6ay\",\n" +
                "                \"properties\": {\n" +
                "                    \"width\": 2352,\n" +
                "                    \"height\": 1088\n" +
                "                },\n" +
                "                \"url\": \"https://s3.arkjp.net/misskey/webpublic-25dea5e7-606c-40f8-af49-db39a39b3eda.png\",\n" +
                "                \"thumbnailUrl\": \"https://s3.arkjp.net/misskey/thumbnail-59e4906d-c198-4cf3-92be-91925a629a1b.jpg\",\n" +
                "                \"comment\": null,\n" +
                "                \"folderId\": null,\n" +
                "                \"folder\": null,\n" +
                "                \"userId\": null,\n" +
                "                \"user\": null\n" +
                "            }\n" +
                "        ],\n" +
                "        \"replyId\": null,\n" +
                "        \"renoteId\": null\n" +
                "    },\n" +
                "    {\n" +
                "        \"id\": \"8ww36oo3vw\",\n" +
                "        \"createdAt\": \"2022-02-18T14:43:35.907Z\",\n" +
                "        \"userId\": \"7rkrarq81i\",\n" +
                "        \"user\": {\n" +
                "            \"id\": \"7rkrarq81i\",\n" +
                "            \"name\": \"しゅいろ\",\n" +
                "            \"username\": \"syuilo\",\n" +
                "            \"host\": null,\n" +
                "            \"avatarUrl\": \"https://s3.arkjp.net/misskey/thumbnail-c7721442-e698-4635-a662-57d78856bbc0.jpg\",\n" +
                "            \"avatarBlurhash\": \"yFF5Kq0L00?a^*IBNG01^j-pV@D*o|xt58WB}@9at7s.Ip~AWB57%Laes:xaOEoLnis:ofIpoJr?NHtRV@oLoeNHNI%1M{kCWCjuxZ\",\n" +
                "            \"avatarColor\": null,\n" +
                "            \"isModerator\": true,\n" +
                "            \"isCat\": true,\n" +
                "            \"emojis\": [],\n" +
                "            \"onlineStatus\": \"online\"\n" +
                "        },\n" +
                "        \"text\": \"カーリング決勝進出:supertada:\",\n" +
                "        \"cw\": null,\n" +
                "        \"visibility\": \"public\",\n" +
                "        \"renoteCount\": 0,\n" +
                "        \"repliesCount\": 0,\n" +
                "        \"reactions\": {\n" +
                "            \"⭐\": 3,\n" +
                "            \"\uD83C\uDF89\": 3,\n" +
                "            \"\uD83C\uDFC5\": 3,\n" +
                "            \"\uD83D\uDC4D\": 4,\n" +
                "            \":blobhai@.:\": 1,\n" +
                "            \":iihanashi@.:\": 2,\n" +
                "            \":supertada@.:\": 3,\n" +
                "            \":supertada@sushi.ski:\": 1,\n" +
                "            \":supertada@friendsyu.me:\": 1,\n" +
                "            \":supertada@kokonect.link:\": 1,\n" +
                "            \":supertada@kr.akirin.xyz:\": 1,\n" +
                "            \":supertada@mk.lei202.com:\": 1\n" +
                "        },\n" +
                "        \"emojis\": [\n" +
                "            {\n" +
                "                \"name\": \"supertada\",\n" +
                "                \"url\": \"https://s3.arkjp.net/misskey/e9794209-1544-47ec-b510-e37bfdd46653\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"blobhai@.\",\n" +
                "                \"url\": \"https://s3.arkjp.net/misskey/b607fc20-fda7-445b-b13f-37f65c3088b0.gif\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"iihanashi@.\",\n" +
                "                \"url\": \"https://s3.arkjp.net/misskey/013cf04e-a057-4aed-ab40-4e0ea97b1aa2.gif\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"supertada@.\",\n" +
                "                \"url\": \"https://s3.arkjp.net/misskey/e9794209-1544-47ec-b510-e37bfdd46653\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"supertada@sushi.ski\",\n" +
                "                \"url\": \"https://misskey.io/proxy/image.png?url=https%3A%2F%2Fmedia.sushi.ski%2Ffiles%2Fc9d25514-0441-451b-9767-f2d565813086.gif\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"supertada@friendsyu.me\",\n" +
                "                \"url\": \"https://misskey.io/proxy/image.png?url=https%3A%2F%2Ffriendsyu.me%2Ffiles%2F603253784d88a20c37091e8a%2F603253784d88a20c37091e8a.gif%3Fweb\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"supertada@kokonect.link\",\n" +
                "                \"url\": \"https://misskey.io/proxy/image.png?url=https%3A%2F%2Fs3.kokonect.link%2Fkokonect%2Ffiles%2F5b58035a-7361-428c-9c07-b93c64a47ef7\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"supertada@kr.akirin.xyz\",\n" +
                "                \"url\": \"https://misskey.io/proxy/image.png?url=https%3A%2F%2Fkr.akirin.xyz%2Ffiles%2F06026776-ef40-47da-9072-cbd4d7095ec8%2F06026776-ef40-47da-9072-cbd4d7095ec8.gif\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"name\": \"supertada@mk.lei202.com\",\n" +
                "                \"url\": \"https://misskey.io/proxy/image.png?url=https%3A%2F%2Fmk.lei202.com%2Ffiles%2F61a24f86acd966bdedae96e6%2F61a24f86acd966bdedae96e6%3Fweb\"\n" +
                "            }\n" +
                "        ],\n" +
                "        \"fileIds\": [],\n" +
                "        \"files\": [],\n" +
                "        \"replyId\": null,\n" +
                "        \"renoteId\": null\n" +
                "    }\n" +
                "]"
        val builder = Json {
            ignoreUnknownKeys = true
        }
        val noteDTO: List<NoteDTO> = builder.decodeFromString(jsonStr)
        Assert.assertNotNull(noteDTO)

    }
}