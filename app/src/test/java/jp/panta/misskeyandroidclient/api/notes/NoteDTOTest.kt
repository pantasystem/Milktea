package jp.panta.misskeyandroidclient.api.notes


import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.pantasystem.milktea.api.misskey.emoji.EmojisType
import net.pantasystem.milktea.api.misskey.emoji.TestNoteObject
import net.pantasystem.milktea.api.misskey.notes.NoteDTO
import net.pantasystem.milktea.model.emoji.Emoji
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class NoteDTOTest {

    @Test
    fun jsonDecodeTest() {
        val builder = Json {
            ignoreUnknownKeys = true
        }
        val jsonStr =
            """{"id":"8lp2oiif5t","createdAt":"2021-05-12T13:38:19.191Z","userId":"8bku1pzti4","user":{"id":"8bku1pzti4","name":"Thor 🇳🇴","username":"thor","host":"pl.thj.no","avatarUrl":"https://nos3.arkjp.net/?url=https%3A%2F%2Fpl.thj.no%2Fmedia%2Fbf68fb5dbe1ca0151a262f5f7c1a99f89c487e2da5c3f1fd6adbb1b871ca31be.jpeg&thumbnail=1","avatarBlurhash":"yNIYC0pyyYt-%#o}J8TK%#.7NytlS5tQIUV?nhxsjEWBV?x]bHbcozM|Rjt6WBkCbHR-R-xuxuIURjxZoeofofNGoyWCt7M{bHofR*","avatarColor":null,"instance":{"name":"Pleroma","softwareName":"pleroma","softwareVersion":"2.3.0-1-gb221d77a","iconUrl":"https://pl.thj.no/favicon.png","faviconUrl":"https://pl.thj.no/favicon.png","themeColor":null},"emojis":[],"onlineStatus":"unknown"},"text":null,"cw":null,"visibility":"public","renoteCount":0,"repliesCount":0,"reactions":{},"emojis":[],"fileIds":[],"files":[],"replyId":null,"renoteId":"8loy77x2cp","uri":"https://pl.thj.no/activities/498b3caf-e819-457b-b5fa-934bf5cd91a9","renote":{"id":"8loy77x2cp","createdAt":"2021-05-12T11:32:53.846Z","userId":"7y4q3ytt21","user":{"id":"7y4q3ytt21","name":"Solid Cанëк :sabakan: ","username":"solidsanek","host":"outerheaven.club","avatarUrl":"https://nos3.arkjp.net/?url=https%3A%2F%2Fouterheaven.club%2Fmedia%2F2b7478fa57151ffde725c6f41b18f215b1b4e6d4769805a89ff6244e0f9ceba3.blob&thumbnail=1","avatarBlurhash":"yiKwR.~Vk=Mxo#xtoz-;IBtQsVkBf7of%gRPxuxas:ofae_3xuM{M|RQW;Rjb^kCn%oyV@f+ofxuoyaeWqV@s:ofWWRjV@j[f6WBWB","avatarColor":null,"instance":{"name":"Outer Heaven","softwareName":"pleroma","softwareVersion":"2.3.0-1-gb221d77a","iconUrl":"https://outerheaven.club/favicon.png","faviconUrl":"https://outerheaven.club/favicon.png","themeColor":null},"emojis":[{"name":"sabakan","url":"https://outerheaven.club/emoji/mess/sabakan.png"}],"onlineStatus":"unknown"},"text":"Lunch time","cw":null,"visibility":"public","renoteCount":6,"repliesCount":1,"reactions":{"👍":7},"emojis":[],"fileIds":["8loy7diyco"],"files":[{"id":"8loy7diyco","createdAt":"2021-05-12T11:33:01.114Z","name":"ff0fb3a916107d7ae0adb95298a9cfd170a70b5265a7204f06bddfd40ab3f3b1.mp4","type":"video/mp4","md5":"2b00b47cd3dce6a0f1da04e6103db676","size":0,"isSensitive":false,"blurhash":null,"properties":{},"url":"https://nos3.arkjp.net/?url=https%3A%2F%2Fouterheaven.club%2Fmedia%2Fff0fb3a916107d7ae0adb95298a9cfd170a70b5265a7204f06bddfd40ab3f3b1.mp4","thumbnailUrl":"https://nos3.arkjp.net/?url=https%3A%2F%2Fouterheaven.club%2Fmedia%2Fff0fb3a916107d7ae0adb95298a9cfd170a70b5265a7204f06bddfd40ab3f3b1.mp4&thumbnail=1","comment":null,"folderId":null,"folder":null,"userId":null,"user":null}],"replyId":null,"renoteId":null,"uri":"https://outerheaven.club/objects/b6f14bd2-8ad1-4e4e-a5c4-ac318a3d61f0"}}"""
        val noteDTO: NoteDTO = builder.decodeFromString(jsonStr)
        Assertions.assertNotNull(noteDTO)
    }

    @Test
    fun jsonDecodeTest2() {
        val jsonStr = """[
    {
        "id": "8wx5r4lrqg",
        "createdAt": "2022-02-19T08:43:15.087Z",
        "userId": "8fu0rxwrdm",
        "user": {
            "id": "8fu0rxwrdm",
            "name": ":_ze::_ro::_za::_su::_ki::wave:",
            "username": "zero_zaki_ghost",
            "host": null,
            "avatarUrl": "https://s3.arkjp.net/misskey/thumbnail-5d26d748-398f-4a68-a876-23e1852f22b1.jpg",
            "avatarBlurhash": "yGL|lz-p4njb-=s+aTRyog?Wa{D:WUWJ~lRjSIofN2j?jM^}M{Wmj@jLogIYI-Ri?GWBV|t7IZ?Zt6S0WVRqWD%3%Joyt6s:n.M{Iq",
            "avatarColor": null,
            "isCat": true,
            "emojis": [
                {
                    "name": "_ze",
                    "url": "https://s3.arkjp.net/misskey/webpublic-fcf3f781-4225-43d5-a5df-53b8936fad4d.png"
                },
                {
                    "name": "_ro",
                    "url": "https://s3.arkjp.net/misskey/webpublic-9b45480c-fa64-4cea-be52-a71c4e67d15a.png"
                },
                {
                    "name": "_za",
                    "url": "https://s3.arkjp.net/misskey/webpublic-d7fc9cef-60eb-4301-91e0-8c74bc41be2a.png"
                },
                {
                    "name": "_su",
                    "url": "https://s3.arkjp.net/misskey/webpublic-91ec1ee3-23d2-42c7-890c-0ef9eed65620.png"
                },
                {
                    "name": "_ki",
                    "url": "https://s3.arkjp.net/misskey/webpublic-ae353d86-489e-4178-b568-89b37ab0c16c.png"
                },
                {
                    "name": "wave",
                    "url": "https://s3.arkjp.net/misskey/webpublic-127746e0-08fe-4e86-be39-1d71a9d35eeb.png"
                }
            ],
            "onlineStatus": "online"
        },
        "text": "いい感じのカレーが来た！🍛",
        "cw": null,
        "visibility": "public",
        "renoteCount": 3,
        "repliesCount": 0,
        "reactions": {
            "🍛": 17,
            "👍": 4,
            "😋": 1,
            ":blobcatdroolreach@.:": 1
        },
        "emojis": [
            {
                "name": "blobcatdroolreach@.",
                "url": "https://s3.arkjp.net/misskey/webpublic-075124a1-5f71-4768-ba9f-8f5e1f5e485a.png"
            }
        ],
        "fileIds": [
            "8wx5qr4in0"
        ],
        "files": [
            {
                "id": "8wx5qr4in0",
                "createdAt": "2022-02-19T08:42:57.618Z",
                "name": "DDB5D7BF-F9AE-4414-980C-22F850477AC4.jpeg",
                "type": "image/jpeg",
                "md5": "6d2a19a2fe519717dcb1bf786a527486",
                "size": 3283785,
                "isSensitive": false,
                "blurhash": "yZHB0R}sRQNGs:xtxasCV@ofxaxGRjWUe=bYt6Rjs:WXNat6s.ofV[a}ofooKa}oJbHfjoLf5aeRkaxofofbHayWV",
                "properties": {
                    "width": 3024,
                    "height": 4032
                },
                "url": "https://s3.arkjp.net/misskey/webpublic-51254b60-edd9-40c5-8c35-e8d6979be41a.jpg",
                "thumbnailUrl": "https://s3.arkjp.net/misskey/thumbnail-d3b5f1f8-93fa-43aa-9e4a-5ea80e259fe9.jpg",
                "comment": null,
                "folderId": null,
                "folder": null,
                "userId": null,
                "user": null
            }
        ],
        "replyId": null,
        "renoteId": null
    },
    {
        "id": "8wx4sqjbld",
        "createdAt": "2022-02-19T08:16:30.551Z",
        "userId": "8fu0rxwrdm",
        "user": {
            "id": "8fu0rxwrdm",
            "name": ":_ze::_ro::_za::_su::_ki::wave:",
            "username": "zero_zaki_ghost",
            "host": null,
            "avatarUrl": "https://s3.arkjp.net/misskey/thumbnail-5d26d748-398f-4a68-a876-23e1852f22b1.jpg",
            "avatarBlurhash": "yGL|lz-p4njb-=s+aTRyog?Wa{D:WUWJ~lRjSIofN2j?jM^}M{Wmj@jLogIYI-Ri?GWBV|t7IZ?Zt6S0WVRqWD%3%Joyt6s:n.M{Iq",
            "avatarColor": null,
            "isCat": true,
            "emojis": [
                {
                    "name": "_ze",
                    "url": "https://s3.arkjp.net/misskey/webpublic-fcf3f781-4225-43d5-a5df-53b8936fad4d.png"
                },
                {
                    "name": "_ro",
                    "url": "https://s3.arkjp.net/misskey/webpublic-9b45480c-fa64-4cea-be52-a71c4e67d15a.png"
                },
                {
                    "name": "_za",
                    "url": "https://s3.arkjp.net/misskey/webpublic-d7fc9cef-60eb-4301-91e0-8c74bc41be2a.png"
                },
                {
                    "name": "_su",
                    "url": "https://s3.arkjp.net/misskey/webpublic-91ec1ee3-23d2-42c7-890c-0ef9eed65620.png"
                },
                {
                    "name": "_ki",
                    "url": "https://s3.arkjp.net/misskey/webpublic-ae353d86-489e-4178-b568-89b37ab0c16c.png"
                },
                {
                    "name": "wave",
                    "url": "https://s3.arkjp.net/misskey/webpublic-127746e0-08fe-4e86-be39-1d71a9d35eeb.png"
                }
            ],
            "onlineStatus": "online"
        },
        "text": "「毒親だけど距離置いてしばらくしたら和解した」って人がちょいちょいおるけど、そーゆう人尊敬する。俺には無理。",
        "cw": null,
        "visibility": "public",
        "renoteCount": 7,
        "repliesCount": 0,
        "reactions": {
            "❤": 1,
            "👍": 7
        },
        "emojis": [],
        "fileIds": [],
        "files": [],
        "replyId": null,
        "renoteId": null
    },
    {
        "id": "8wx4j1g676",
        "createdAt": "2022-02-19T08:08:58.134Z",
        "userId": "7rkrg1wo1a",
        "user": {
            "id": "7rkrg1wo1a",
            "name": "村上さん",
            "username": "AureoleArk",
            "host": null,
            "avatarUrl": "https://s3.arkjp.net/misskey/thumbnail-80626a47-0654-4863-98e1-a7ecfb1c7131.jpg",
            "avatarBlurhash": "yHM8pYx]0n-TJCt89vLNEk+sadNK=vxYIUf+o*EQAJWqT0%1RjDjI[%LS5NeRkNIt.i_NGbbv|SixujERkWYXTa#r;jY",
            "avatarColor": null,
            "isModerator": true,
            "emojis": [],
            "onlineStatus": "unknown"
        },
        "text": null,
        "cw": null,
        "visibility": "public",
        "renoteCount": 4,
        "repliesCount": 0,
        "reactions": {
            "👍": 1,
            "😇": 4,
            "🥴": 4,
            ":bap@.:": 2
        },
        "emojis": [
            {
                "name": "bap@.",
                "url": "https://s3.arkjp.net/misskey/1a8e89a6-10fd-4315-85ad-491bd1144058.gif"
            }
        ],
        "fileIds": [
            "8wx4j0frcs"
        ],
        "files": [
            {
                "id": "8wx4j0frcs",
                "createdAt": "2022-02-19T08:08:56.823Z",
                "name": "2022-02-19 17-08-56 1.png",
                "type": "image/png",
                "md5": "f04366a251295387280bc0788293f0b4",
                "size": 384761,
                "isSensitive": false,
                "blurhash": "yLAB6UaI9GnMnes8rByGjFRjjEjDnjD00tS?GkEj_bJXoL}oz%2kEbeX9XU^%WAE3j]kWW=nNxvocNKodt5oIngD*ofxtaejEslbc",
                "properties": {
                    "width": 615,
                    "height": 737
                },
                "url": "https://s3.arkjp.net/misskey/webpublic-574ad5eb-66d6-42b7-b59f-e99a744dc140.png",
                "thumbnailUrl": "https://s3.arkjp.net/misskey/thumbnail-b8ec2fd3-3b55-4c0e-bb2c-2a38e966a190.jpg",
                "comment": null,
                "folderId": "7v9lb3aif9",
                "folder": null,
                "userId": null,
                "user": null
            }
        ],
        "replyId": null,
        "renoteId": null
    },
    {
        "id": "8wx4at7wy5",
        "createdAt": "2022-02-19T08:02:34.220Z",
        "userId": "8g0j5lv00n",
        "user": {
            "id": "8g0j5lv00n",
            "name": "t_w 79.9kg",
            "username": "t_w",
            "host": null,
            "avatarUrl": "https://s3.arkjp.net/misskey/thumbnail-bfc014aa-76d8-45eb-87a5-8db3e5c1baa1.png",
            "avatarBlurhash": "yUO9t7${'$'}+xaD~SO9ZxuxaVtV[%MoyWBbbIVIUsp%LxtofNGX7ozRjI:W=oc-pxtNGf+t7WBxas.oybFRjxtoKWCj]WAoLWV",
            "avatarColor": null,
            "emojis": [],
            "onlineStatus": "online"
        },
        "text": "Rustで一家離散させて遊んでる",
        "cw": null,
        "visibility": "public",
        "renoteCount": 7,
        "repliesCount": 2,
        "reactions": {
            "🎉": 1,
            "👍": 4,
            ":shikei@.:": 4
        },
        "emojis": [
            {
                "name": "shikei@.",
                "url": "https://s3.arkjp.net/misskey/webpublic-01d065e5-a99e-455b-b449-f96b4a79fb52.png"
            }
        ],
        "fileIds": [
            "8wx4altv55"
        ],
        "files": [
            {
                "id": "8wx4altv55",
                "createdAt": "2022-02-19T08:02:24.643Z",
                "name": "2022-02-19 17-02-24 1.png",
                "type": "image/png",
                "md5": "ce5b2ed518a903daae3ff57736c2ba91",
                "size": 37629,
                "isSensitive": false,
                "blurhash": "y05OQl_$'}#ICWtE7IXNfI=I;00${'$'}*s;Vu%1j=j?~qNYI.NFM{ayV[%MM|t8t7R*R*ba00xvxbs;s:RkRj_3tPR%t6R%RjR+",
                "properties": {
                    "width": 462,
                    "height": 542
                },
                "url": "https://s3.arkjp.net/misskey/webpublic-50ae16b4-b466-4b67-ad52-5fb712dcae17.png",
                "thumbnailUrl": "https://s3.arkjp.net/misskey/thumbnail-129337e4-7233-4c5e-8a0c-fb425208b3a5.jpg",
                "comment": null,
                "folderId": null,
                "folder": null,
                "userId": null,
                "user": null
            }
        ],
        "replyId": null,
        "renoteId": null
    },
    {
        "id": "8wx3kz489a",
        "createdAt": "2022-02-19T07:42:28.808Z",
        "userId": "7rkrg1wo1a",
        "user": {
            "id": "7rkrg1wo1a",
            "name": "村上さん",
            "username": "AureoleArk",
            "host": null,
            "avatarUrl": "https://s3.arkjp.net/misskey/thumbnail-80626a47-0654-4863-98e1-a7ecfb1c7131.jpg",
            "avatarBlurhash": "yHM8pYx]0n-TJCt89vLNEk+sadNK=vxYIUf+QAJWqT0%1RjDjI[%LS5NeRkNIt.i_NGbbv|SixujERkWYXTa#r;jY",
            "avatarColor": null,
            "isModerator": true,
            "emojis": [],
            "onlineStatus": "unknown"
        },
        "text": "Misskeyの完全なSVGロゴなかったので作った\n（公式のSVGはPNGがbase64に変換されたものが埋め込まれている）\n\nhttps://s3.arkjp.net/misskey/3219e602-a0fe-42c7-aa35-f39ddcf90fc2",
        "cw": null,
        "visibility": "public",
        "renoteCount": 10,
        "repliesCount": 0,
        "reactions": {
            "🎉": 1,
            "😊": 1,
            "🥰": 1,
            ":igyo@.:": 1,
            ":misskey@.:": 4,
            ":nacho_hi@.:": 1,
            ":arigatofes@.:": 1,
            ":kami@nca10.net:": 1,
            ":misskey@fedibird.com:": 1,
            ":iihanashi@umaskey.net:": 1,
            ":blob_hearteyes@sushi.ski:": 1,
            ":igyou@misky.rikunagiweb.jp:": 1
        },
        "emojis": [
            {
                "name": "igyo@.",
                "url": "https://s3.arkjp.net/misskey/webpublic-d50d9d65-8413-4236-9762-393e4f3585ce.png"
            },
            {
                "name": "misskey@.",
                "url": "https://s3.arkjp.net/misskey/webpublic-f3ca12a3-0254-4326-aac8-b79915fc34d6.png"
            },
            {
                "name": "nacho_hi@.",
                "url": "https://s3.arkjp.net/misskey/webpublic-105da421-b3af-48fa-af6f-d616f9510823.png"
            },
            {
                "name": "arigatofes@.",
                "url": "https://s3.arkjp.net/misskey/webpublic-ddbfee9d-91ab-419a-9247-64dd90b62fac.png"
            },
            {
                "name": "kami@nca10.net",
                "url": "https://misskey.io/proxy/image.png?url=https%3A%2F%2Fs3.nca10.net%2Fmisskey%2Fwebpublic-1e162464-dbd5-48ce-bd01-39d12e5082ed.png"
            },
            {
                "name": "hosii@nca10.net",
                "url": "https://misskey.io/proxy/image.png?url=https%3A%2F%2Fs3.nca10.net%2Fmisskey%2Fwebpublic-ef879915-4a38-43f1-8d64-84c763bbbeae.png"
            },
            {
                "name": "misskey@fedibird.com",
                "url": "https://misskey.io/proxy/image.png?url=https%3A%2F%2Fs3.fedibird.com%2Fcustom_emojis%2Fimages%2F000%2F008%2F417%2Foriginal%2Fb081a4cecfbf0750.png"
            },
            {
                "name": "iihanashi@umaskey.net",
                "url": "https://misskey.io/proxy/image.png?url=https%3A%2F%2Fumaskey.net%2Ffiles%2F9249dae6-22f8-4536-9723-3ef10a1e6b66"
            },
            {
                "name": "blob_hearteyes@sushi.ski",
                "url": "https://misskey.io/proxy/image.png?url=https%3A%2F%2Fmedia.sushi.ski%2Ffiles%2Fd013b1a9-8c40-48d3-827f-53422814419d.gif"
            },
            {
                "name": "igyou@misky.rikunagiweb.jp",
                "url": "https://misskey.io/proxy/image.png?url=https%3A%2F%2Fmedia-misky.rikunagiweb.jp%2Fmedia%2Fwebpublic-9244077e-df15-4c9f-a02f-5ad4d25fe3bd.png"
            }
        ],
        "fileIds": [
            "8wx3hl69v7"
        ],
        "files": [
            {
                "id": "8wx3hl69v7",
                "createdAt": "2022-02-19T07:39:50.769Z",
                "name": "mi.svg",
                "type": "image/svg+xml",
                "md5": "2167b6ce26cd2c1d78530562f0a4188a",
                "size": 5837,
                "isSensitive": false,
                "blurhash": "yHE;,e%r.g%sy8MkM#yAWCWYoJj[V_af8,M*th%bkBM%RTtNV^t5oej@j@WCRBV]oxf7kAj@ayagayovovkAazk9RSoxRSRTagkAow",
                "properties": {
                    "width": 1345.9,
                    "height": 985.05
                },
                "url": "https://s3.arkjp.net/misskey/webpublic-597033c8-49aa-4adb-bbc6-d025e85c2df4.png",
                "thumbnailUrl": "https://s3.arkjp.net/misskey/thumbnail-b34d0613-c969-40f7-a529-39725519e736.png",
                "comment": null,
                "folderId": "7v9lb3aif9",
                "folder": null,
                "userId": null,
                "user": null
            }
        ],
        "replyId": null,
        "renoteId": null
    },
    {
        "id": "8wx3dgiiss",
        "createdAt": "2022-02-19T07:36:38.106Z",
        "userId": "8fu0rxwrdm",
        "user": {
            "id": "8fu0rxwrdm",
            "name": ":_ze::_ro::_za::_su::_ki::wave:",
            "username": "zero_zaki_ghost",
            "host": null,
            "avatarUrl": "https://s3.arkjp.net/misskey/thumbnail-5d26d748-398f-4a68-a876-23e1852f22b1.jpg",
            "avatarBlurhash": "yGL|lz-p4njb-=s+aTRyog?Wa{D:WUWJ~lRjSIofN2j?jM^}M{Wmj@jLogIYI-Ri?GWBV|t7IZ?Zt6S0WVRqWD%3%Joyt6s:n.M{Iq",
            "avatarColor": null,
            "isCat": true,
            "emojis": [
                {
                    "name": "_ze",
                    "url": "https://s3.arkjp.net/misskey/webpublic-fcf3f781-4225-43d5-a5df-53b8936fad4d.png"
                },
                {
                    "name": "_ro",
                    "url": "https://s3.arkjp.net/misskey/webpublic-9b45480c-fa64-4cea-be52-a71c4e67d15a.png"
                },
                {
                    "name": "_za",
                    "url": "https://s3.arkjp.net/misskey/webpublic-d7fc9cef-60eb-4301-91e0-8c74bc41be2a.png"
                },
                {
                    "name": "_su",
                    "url": "https://s3.arkjp.net/misskey/webpublic-91ec1ee3-23d2-42c7-890c-0ef9eed65620.png"
                },
                {
                    "name": "_ki",
                    "url": "https://s3.arkjp.net/misskey/webpublic-ae353d86-489e-4178-b568-89b37ab0c16c.png"
                },
                {
                    "name": "wave",
                    "url": "https://s3.arkjp.net/misskey/webpublic-127746e0-08fe-4e86-be39-1d71a9d35eeb.png"
                }
            ],
            "onlineStatus": "online"
        },
        "text": "それでは〜？ぺーい！🍻",
        "cw": null,
        "visibility": "public",
        "renoteCount": 1,
        "repliesCount": 1,
        "reactions": {
            "🍻": 14,
            "👍": 3
        },
        "emojis": [],
        "fileIds": [
            "8wx3dbuk47"
        ],
        "files": [
            {
                "id": "8wx3dbuk47",
                "createdAt": "2022-02-19T07:36:32.060Z",
                "name": "D1D2514D-8FF8-4524-ABB4-2CF8235E60A7.jpeg",
                "type": "image/jpeg",
                "md5": "ddefed5454d0288f234799d700f69d9f",
                "size": 3300034,
                "isSensitive": false,
                "blurhash": "y9I4CDD+?a57-U-VIp~BE3Sj%1M|IqR*IWE2WBRjWBRkbGNdt7%1t6ofs:t6%1R+I;xss:R+X7xDWBR+NGWBNIofoft6f*xss:s:WU",
                "properties": {
                    "width": 3024,
                    "height": 4032
                },
                "url": "https://s3.arkjp.net/misskey/webpublic-71a17c67-a2ea-41e3-9102-4c2040528864.jpg",
                "thumbnailUrl": "https://s3.arkjp.net/misskey/thumbnail-8d5611f8-36d2-402c-8cc6-7e10f8e9d3f6.jpg",
                "comment": null,
                "folderId": null,
                "folder": null,
                "userId": null,
                "user": null
            }
        ],
        "replyId": null,
        "renoteId": null
    },
    {
        "id": "8wwwqjdrhi",
        "createdAt": "2022-02-19T04:30:51.039Z",
        "userId": "7rkrarq81i",
        "user": {
            "id": "7rkrarq81i",
            "name": "しゅいろ",
            "username": "syuilo",
            "host": null,
            "avatarUrl": "https://s3.arkjp.net/misskey/thumbnail-c7721442-e698-4635-a662-57d78856bbc0.jpg",
            "avatarBlurhash": "yFF5Kq0L00?a^*IBNG01^j-pV@D*o|xt58WB}@9at7s.Ip~AWB57%Laes:xaOEoLnis:ofIpoJr?NHtRV@oLoeNHNI%1M{kCWCjuxZ",
            "avatarColor": null,
            "isModerator": true,
            "isCat": true,
            "emojis": [],
            "onlineStatus": "online"
        },
        "text": "コロニャの影響でコンビニ閉まってた:sonnakotoarunda:",
        "cw": null,
        "visibility": "public",
        "renoteCount": 5,
        "repliesCount": 0,
        "reactions": {
            "⭐": 4,
            "👍": 6,
            "😥": 4,
            "😮": 2,
            "🤔": 1,
            ":murishite@.:": 1,
            ":maanantekoto@.:": 1,
            ":omae_ga_tsukure@.:": 1,
            ":sonnakotoarunda@.:": 5,
            ":nacho_cry@nya.one:": 1,
            ":ablobcatblinkhyper@.:": 1,
            ":ablobcatcryingcute@.:": 2,
            ":crying_cat@nca10.net:": 1,
            ":blob_sleepy@sushi.ski:": 1,
            ":blobsleepless@k.lapy.link:": 1,
            ":cyber_hacking@mk.f72u.net:": 1,
            ":blobcatcry@misskey.m544.net:": 1,
            ":sonnakotoarunda@friendsyu.me:": 1,
            ":sonnakotoarunda@mk.lei202.com:": 1
        },
        "emojis": [
            {
                "name": "sonnakotoarunda",
                "url": "https://s3.arkjp.net/misskey/webpublic-52ee9700-7114-4690-88cf-8633b2ad962a.png"
            },
            {
                "name": "murishite@.",
                "url": "https://s3.arkjp.net/misskey/webpublic-1f9050ad-bdc8-4c86-8c07-68f70f55f887.png"
            },
            {
                "name": "maanantekoto@.",
                "url": "https://s3.arkjp.net/misskey/webpublic-15ec5fe4-42ec-41f8-8cd8-fdfafefea93c.png"
            },
            {
                "name": "omae_ga_tsukure@.",
                "url": "https://s3.arkjp.net/misskey/webpublic-0c0c7db8-359b-4caf-ae95-5e9a47312f42.png"
            },
            {
                "name": "sonnakotoarunda@.",
                "url": "https://s3.arkjp.net/misskey/webpublic-52ee9700-7114-4690-88cf-8633b2ad962a.png"
            },
            {
                "name": "nacho_cry@nya.one",
                "url": "https://misskey.io/proxy/image.png?url=https%3A%2F%2Ffile.nya.one%2Fmisskey%2Fwebpublic-494cfc89-a3c7-46a5-95d8-fb362bacd3ba.png"
            },
            {
                "name": "ablobcatblinkhyper@.",
                "url": "https://s3.arkjp.net/misskey/c1d1be32-f3c3-4c60-9509-cb690b7935a8"
            },
            {
                "name": "ablobcatcryingcute@.",
                "url": "https://s3.arkjp.net/misskey/b90b23ac-432d-4bd2-9e58-8ee6980f5595.apng"
            },
            {
                "name": "crying_cat@nca10.net",
                "url": "https://misskey.io/proxy/image.png?url=https%3A%2F%2Fs3.nca10.net%2Fmisskey%2Fc5f30fa7-2459-47eb-a9ce-060bf6c617d6.apng"
            },
            {
                "name": "blob_sleepy@sushi.ski",
                "url": "https://misskey.io/proxy/image.png?url=https%3A%2F%2Fmedia.sushi.ski%2Ffiles%2F3f6f4034-f527-4e3a-bbb6-406dbcefb9b1.gif"
            },
            {
                "name": "blobsleepless@k.lapy.link",
                "url": "https://misskey.io/proxy/image.png?url=https%3A%2F%2Fmisskeylapy.s3.amazonaws.com%2Fnull%2F6fe193f7-a30e-48ad-abba-04f7e92c597e.png"
            },
            {
                "name": "cyber_hacking@mk.f72u.net",
                "url": "https://misskey.io/proxy/image.png?url=https%3A%2F%2Fmk.f72u.net%2Fmedia%2Fmisskey%2F88c357e5-0c30-43c9-9cf5-adb009e0a916.gif"
            },
            {
                "name": "blobcatcry@misskey.m544.net",
                "url": "https://misskey.io/proxy/image.png?url=https%3A%2F%2Fmisskey-drive2.m544.net%2Fm544%2Fzcj6iwsig8iw4c9r29cegarn.png"
            },
            {
                "name": "sonnakotoarunda@friendsyu.me",
                "url": "https://misskey.io/proxy/image.png?url=https%3A%2F%2Ffriendsyu.me%2Ffiles%2F602edca04d88a20c3708cd38%2F602edca04d88a20c3708cd38.png%3Fweb"
            },
            {
                "name": "sonnakotoarunda@mk.lei202.com",
                "url": "https://misskey.io/proxy/image.png?url=https%3A%2F%2Fmk.lei202.com%2Ffiles%2F6136515418e61a041d63583e%2F6136515418e61a041d63583e.png%3Fweb"
            }
        ],
        "fileIds": [
            "8wwwpv6ex6"
        ],
        "files": [
            {
                "id": "8wwwpv6ex6",
                "createdAt": "2022-02-19T04:30:19.670Z",
                "name": "D556EB85-FF92-4592-B5F4-909F221C98D4.jpeg",
                "type": "image/jpeg",
                "md5": "f20b1343b469a36ac0e7c5736926a406",
                "size": 4090988,
                "isSensitive": false,
                "blurhash": "yRG+UNM{ads:E1ofM|_Nt5adIUjYRjRj?cIVM_aeM|Rjay.9M{V?M{WAocRj?bayRjaeoJRkjZ?bj[ofj?Rjofax%MWAfQayj@oeRj",
                "properties": {
                    "width": 4032,
                    "height": 3024
                },
                "url": "https://s3.arkjp.net/misskey/webpublic-04603751-9ed4-44e9-81a8-462f31ce6cde.jpg",
                "thumbnailUrl": "https://s3.arkjp.net/misskey/thumbnail-9ea18611-136c-4443-b782-293a627c7a97.jpg",
                "comment": null,
                "folderId": null,
                "folder": null,
                "userId": null,
                "user": null
            }
        ],
        "replyId": null,
        "renoteId": null
    },
    {
        "id": "8wwscscb12",
        "createdAt": "2022-02-19T02:28:11.003Z",
        "userId": "86p1tmjaav",
        "user": {
            "id": "86p1tmjaav",
            "name": "みれい (1,650km・5.9E+16t)",
            "username": "Mi",
            "host": null,
            "avatarUrl": "https://s3.arkjp.net/misskey/thumbnail-6c009d3c-e7cd-4f55-96a4-2f1b99670f4c.jpg",
            "avatarBlurhash": "yiKLUEof*0kW.8t7RjI;ay-pofIUWBoex^ayayM{bIadWBjZofjsV?bHbHWBtRxvfPoKbHWBayoft7f6RjbHt7ayjt",
            "avatarColor": null,
            "emojis": [],
            "onlineStatus": "online"
        },
        "text": "1,650kmのみれいさん Vs. 日本列島",
        "cw": null,
        "visibility": "public",
        "renoteCount": 8,
        "repliesCount": 3,
        "reactions": {
            "👍": 4,
            "🤔": 1,
            ":cyber_hacking@.:": 2,
            ":ablobdundundun@.:": 2,
            ":confusedparrot@fedibird.com:": 1,
            ":octothink@misky.rikunagiweb.jp:": 1
        },
        "emojis": [
            {
                "name": "cyber_hacking@.",
                "url": "https://s3.arkjp.net/misskey/f4ace218-272f-49e7-8813-66f47db9efba"
            },
            {
                "name": "ablobdundundun@.",
                "url": "https://s3.arkjp.net/misskey/c62c5cef-b9c0-4d8f-b0ce-2e2a358abe9a.gif"
            },
            {
                "name": "confusedparrot@fedibird.com",
                "url": "https://misskey.io/proxy/image.png?url=https%3A%2F%2Fs3.fedibird.com%2Fcustom_emojis%2Fimages%2F000%2F049%2F092%2Foriginal%2Fdbdc78a4a85a4b50.gif"
            },
            {
                "name": "octothink@misky.rikunagiweb.jp",
                "url": "https://misskey.io/proxy/image.png?url=https%3A%2F%2Fmedia-misky.rikunagiweb.jp%2Fmedia%2Fwebpublic-9511a7bf-1cbd-46c5-a0f5-481f8617d1e1.png"
            }
        ],
        "fileIds": [
            "8wwsccpzeq"
        ],
        "files": [
            {
                "id": "8wwsccpzeq",
                "createdAt": "2022-02-19T02:27:50.759Z",
                "name": "みれいさん.png",
                "type": "image/png",
                "md5": "cfd22a363a7829a356f860499e636081",
                "size": 2034258,
                "isSensitive": true,
                "blurhash": "y97K|^_LM{IoofWCaz-.xsoet7s:RjRjR%axs:oNoftQoxtQt7RkIVNFt7t7IVayxukCNGWBj[ayj?jZWBoft7f8xtofR%bFoyf8a#",
                "properties": {
                    "width": 1658,
                    "height": 981
                },
                "url": "https://s3.arkjp.net/misskey/webpublic-fec77b5e-f322-47b8-b063-e1830c835f9f.png",
                "thumbnailUrl": "https://s3.arkjp.net/misskey/thumbnail-0c309337-dcb1-44a5-bd78-8e4b625adacb.png",
                "comment": null,
                "folderId": null,
                "folder": null,
                "userId": null,
                "user": null
            }
        ],
        "replyId": null,
        "renoteId": null
    },
    {
        "id": "8ww3dhb07s",
        "createdAt": "2022-02-18T14:48:52.956Z",
        "userId": "7rkrarq81i",
        "user": {
            "id": "7rkrarq81i",
            "name": "しゅいろ",
            "username": "syuilo",
            "host": null,
            "avatarUrl": "https://s3.arkjp.net/misskey/thumbnail-c7721442-e698-4635-a662-57d78856bbc0.jpg",
            "avatarBlurhash": "yFF5Kq0L00?a^*IBNG01^j-pV@D*o|xt58WB}@9at7s.Ip~AWB57%Laes:xaOEoLnis:ofIpoJr?NHtRV@oLoeNHNI%1M{kCWCjuxZ",
            "avatarColor": null,
            "isModerator": true,
            "isCat": true,
            "emojis": [],
            "onlineStatus": "online"
        },
        "text": "テストが3時間経っても終わらにゃいの:ijo:",
        "cw": null,
        "visibility": "public",
        "renoteCount": 3,
        "repliesCount": 0,
        "reactions": {
            "⭐": 6,
            "👍": 5,
            ":ijo@.:": 3,
            ":yabaiwayo@.:": 1,
            ":senko_stop@.:": 1,
            ":ijo@sushi.ski:": 1,
            ":issue@msk.minetaro12.com:": 1
        },
        "emojis": [
            {
                "name": "ijo",
                "url": "https://s3.arkjp.net/misskey/webpublic-085c1e56-1157-4c7a-9b93-b43be4f7d7ef.png"
            },
            {
                "name": "ijo@.",
                "url": "https://s3.arkjp.net/misskey/webpublic-085c1e56-1157-4c7a-9b93-b43be4f7d7ef.png"
            },
            {
                "name": "yabaiwayo@.",
                "url": "https://s3.arkjp.net/misskey/webpublic-4c3e962c-2b2a-48fa-845b-a0822eef733d.png"
            },
            {
                "name": "senko_stop@.",
                "url": "https://s3.arkjp.net/misskey/webpublic-d600ad33-561c-4f39-a103-9772529395a9.png"
            },
            {
                "name": "ijo@sushi.ski",
                "url": "https://misskey.io/proxy/image.png?url=https%3A%2F%2Fmedia.sushi.ski%2Ffiles%2Fwebpublic-3678f3f9-727d-4d07-85b0-b9d46d9cd88f.png"
            },
            {
                "name": "issue@msk.minetaro12.com",
                "url": "https://misskey.io/proxy/image.png?url=https%3A%2F%2Fmsk.minetaro12.com%2Ffiles%2Fwebpublic-ef33fbde-c155-44a4-a529-ff4515d01873"
            }
        ],
        "fileIds": [
            "8ww3cxdn8i"
        ],
        "files": [
            {
                "id": "8ww3cxdn8i",
                "createdAt": "2022-02-18T14:48:27.131Z",
                "name": "スクリーンショット 2022-02-18 23.47.40.png",
                "type": "image/png",
                "md5": "c6d0fec428c2a5cc0f8640a3b89b9660",
                "size": 264526,
                "isSensitive": false,
                "blurhash": "y01.]T%NRiadV?ofofozt7%gt8xuWAM{ITt8x]j]WBWBfjV[bJM{ogWBWCt7M{t8RkRPofayj]xvaxt7ozWAaxRj%Noet7jYaef6ay",
                "properties": {
                    "width": 2352,
                    "height": 1088
                },
                "url": "https://s3.arkjp.net/misskey/webpublic-25dea5e7-606c-40f8-af49-db39a39b3eda.png",
                "thumbnailUrl": "https://s3.arkjp.net/misskey/thumbnail-59e4906d-c198-4cf3-92be-91925a629a1b.jpg",
                "comment": null,
                "folderId": null,
                "folder": null,
                "userId": null,
                "user": null
            }
        ],
        "replyId": null,
        "renoteId": null
    },
    {
        "id": "8ww36oo3vw",
        "createdAt": "2022-02-18T14:43:35.907Z",
        "userId": "7rkrarq81i",
        "user": {
            "id": "7rkrarq81i",
            "name": "しゅいろ",
            "username": "syuilo",
            "host": null,
            "avatarUrl": "https://s3.arkjp.net/misskey/thumbnail-c7721442-e698-4635-a662-57d78856bbc0.jpg",
            "avatarBlurhash": "yFF5Kq0L00?a^*IBNG01^j-pV@D*o|xt58WB}@9at7s.Ip~AWB57%Laes:xaOEoLnis:ofIpoJr?NHtRV@oLoeNHNI%1M{kCWCjuxZ",
            "avatarColor": null,
            "isModerator": true,
            "isCat": true,
            "emojis": [],
            "onlineStatus": "online"
        },
        "text": "カーリング決勝進出:supertada:",
        "cw": null,
        "visibility": "public",
        "renoteCount": 0,
        "repliesCount": 0,
        "reactions": {
            "⭐": 3,
            "🎉": 3,
            "🏅": 3,
            "👍": 4,
            ":blobhai@.:": 1,
            ":iihanashi@.:": 2,
            ":supertada@.:": 3,
            ":supertada@sushi.ski:": 1,
            ":supertada@friendsyu.me:": 1,
            ":supertada@kokonect.link:": 1,
            ":supertada@kr.akirin.xyz:": 1,
            ":supertada@mk.lei202.com:": 1
        },
        "emojis": [
            {
                "name": "supertada",
                "url": "https://s3.arkjp.net/misskey/e9794209-1544-47ec-b510-e37bfdd46653"
            },
            {
                "name": "blobhai@.",
                "url": "https://s3.arkjp.net/misskey/b607fc20-fda7-445b-b13f-37f65c3088b0.gif"
            },
            {
                "name": "iihanashi@.",
                "url": "https://s3.arkjp.net/misskey/013cf04e-a057-4aed-ab40-4e0ea97b1aa2.gif"
            },
            {
                "name": "supertada@.",
                "url": "https://s3.arkjp.net/misskey/e9794209-1544-47ec-b510-e37bfdd46653"
            },
            {
                "name": "supertada@sushi.ski",
                "url": "https://misskey.io/proxy/image.png?url=https%3A%2F%2Fmedia.sushi.ski%2Ffiles%2Fc9d25514-0441-451b-9767-f2d565813086.gif"
            },
            {
                "name": "supertada@friendsyu.me",
                "url": "https://misskey.io/proxy/image.png?url=https%3A%2F%2Ffriendsyu.me%2Ffiles%2F603253784d88a20c37091e8a%2F603253784d88a20c37091e8a.gif%3Fweb"
            },
            {
                "name": "supertada@kokonect.link",
                "url": "https://misskey.io/proxy/image.png?url=https%3A%2F%2Fs3.kokonect.link%2Fkokonect%2Ffiles%2F5b58035a-7361-428c-9c07-b93c64a47ef7"
            },
            {
                "name": "supertada@kr.akirin.xyz",
                "url": "https://misskey.io/proxy/image.png?url=https%3A%2F%2Fkr.akirin.xyz%2Ffiles%2F06026776-ef40-47da-9072-cbd4d7095ec8%2F06026776-ef40-47da-9072-cbd4d7095ec8.gif"
            },
            {
                "name": "supertada@mk.lei202.com",
                "url": "https://misskey.io/proxy/image.png?url=https%3A%2F%2Fmk.lei202.com%2Ffiles%2F61a24f86acd966bdedae96e6%2F61a24f86acd966bdedae96e6%3Fweb"
            }
        ],
        "fileIds": [],
        "files": [],
        "replyId": null,
        "renoteId": null
    }
]"""
        val builder = Json {
            ignoreUnknownKeys = true
        }
        val noteDTO: List<NoteDTO> = builder.decodeFromString(jsonStr)
        Assertions.assertNotNull(noteDTO)

    }

    @Test
    fun decodeArrayTypeEmoji() {
        val builder = Json {
            ignoreUnknownKeys = true
        }
        val json1 = """
            [{"name": "hoge", "url": "https://example.com"}]
        """.trimIndent()
        val result = builder.decodeFromString<EmojisType>(json1)
        Assertions.assertEquals(EmojisType.TypeArray(listOf(Emoji(name = "hoge", url = "https://example.com"))), result)

    }

    @Test
    fun decodeObjectTypeEmoji() {
        val builder = Json {
            ignoreUnknownKeys = true
        }
        val json1 = """
            {"hoge": "https://example.com", "piyo": "https://misskey.io"}
        """.trimIndent()
        val result = builder.decodeFromString<EmojisType>(json1)

        Assertions.assertEquals(EmojisType.TypeObject(mapOf("hoge" to "https://example.com", "piyo" to "https://misskey.io")), result)
    }

    @Test
    fun decodeFunckingObject() {
        val builder = Json {
            ignoreUnknownKeys = true
        }
        val json1 = """
            {"emojis": [{"name": "hoge", "url": "https://example.com"}]}
        """.trimIndent()
        val result = builder.decodeFromString<TestNoteObject>(json1)
        Assertions.assertEquals(TestNoteObject(EmojisType.TypeArray(
            listOf(Emoji(name = "hoge", url = "https://example.com"))
        )), result)
    }

    @Test
    fun decodeEmptyObject() {
        val builder = Json {
            ignoreUnknownKeys = true
        }
        val json1 = """
            {"emojis": {}}
        """.trimIndent()
        val result = builder.decodeFromString<TestNoteObject>(json1)

        Assertions.assertEquals(TestNoteObject(EmojisType.TypeObject(emptyMap())), result)
    }
}