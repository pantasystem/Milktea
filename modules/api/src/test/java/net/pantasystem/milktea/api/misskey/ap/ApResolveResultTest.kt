package net.pantasystem.milktea.api.misskey.ap

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ApResolveResultTest {

    @Test
    fun decodeGiveTypeNote() {
        val json = """
            {
                "type": "Note",
                "object": {
                    "id": "7wkp7bjzl1",
                    "createdAt": "2019-08-18T15:37:18.911Z",
                    "userId": "919yl2fdkn",
                    "user": {
                        "id": "919yl2fdkn",
                        "name": "harunon:keybase:さんをフォローしている人はこんな投稿もいいねしています",
                        "username": "harunon",
                        "host": "misskey.io",
                        "avatarUrl": "https://misskey.pantasystem.com/files/thumbnail-d9518707-06cf-474b-8680-2cf97cfb06f9",
                        "avatarBlurhash": "y8G]g]%NrOIu-n^+0LTKt6={xZE3Ipjs03j?ki%2E1IV^+,0M|0MIp^%-oog~VD+SukB-Ws:E1-;xt^*xa9aR*M|Jj?arvoINGNHX7",
                        "avatarColor": null,
                        "isCat": true,
                        "instance": {
                            "name": "Misskey.io",
                            "softwareName": "misskey",
                            "softwareVersion": "12.119.0",
                            "iconUrl": "https://misskey.io/static-assets/icons/192.png",
                            "faviconUrl": "https://s3.arkjp.net/misskey/webpublic-0c66b1ca-b8c0-4eaa-9827-47674f4a1580.png",
                            "themeColor": "#86b300"
                        },
                        "emojis": [
                            {
                                "name": "keybase",
                                "url": "https://misskey.pantasystem.com/proxy/%2Fmisskey%2Fwebpublic-9b522253-9932-45a1-8789-329ad4915e80.png?url=https%3A%2F%2Fs3.arkjp.net%2Fmisskey%2Fwebpublic-9b522253-9932-45a1-8789-329ad4915e80.png"
                            }
                        ],
                        "onlineStatus": "unknown",
                        "driveCapacityOverrideMb": null
                    },
                    "text": "ヒモを養うかきくけこ\n\nカードにゃらいくらでも使っていいよ\n聞いたこと無いよ、この請求書\n苦しいときは、私にいってね\n計画的にお金は使ってね\n今月のお小遣い、ここにおいておくから",
                    "cw": null,
                    "visibility": "public",
                    "renoteCount": 2,
                    "repliesCount": 0,
                    "reactions": {
                        "💛": 1,
                        ":ijo@misskey.io:": 1
                    },
                    "emojis": [
                        {
                            "name": "ijo@misskey.io",
                            "url": "https://misskey.pantasystem.com/proxy/%2Fmisskey%2Fwebpublic-085c1e56-1157-4c7a-9b93-b43be4f7d7ef.png?url=https%3A%2F%2Fs3.arkjp.net%2Fmisskey%2Fwebpublic-085c1e56-1157-4c7a-9b93-b43be4f7d7ef.png"
                        }
                    ],
                    "fileIds": [],
                    "files": [],
                    "replyId": null,
                    "renoteId": null,
                    "uri": "https://misskey.io/notes/7wkp7bjzdz"
                }
            }
        """.trimIndent()

        val decoder = Json {
            ignoreUnknownKeys = true
        }
        val result: ApResolveResult = decoder.decodeFromString(json)
        Assertions.assertEquals(
            "ヒモを養うかきくけこ\n" +
                    "\n" +
                    "カードにゃらいくらでも使っていいよ\n" +
                    "聞いたこと無いよ、この請求書\n" +
                    "苦しいときは、私にいってね\n" +
                    "計画的にお金は使ってね\n" +
                    "今月のお小遣い、ここにおいておくから", (result as ApResolveResult.TypeNote).note.text
        )
    }

    @Test
    fun decodeGiveTypeUser() {
        val json = """
            {
                "type": "User",
                "object": {
                    "id": "919zdqcdzy",
                    "name": "まりふぁいあ🔥",
                    "username": "refia",
                    "host": "misskey.io",
                    "avatarUrl": "https://misskey.pantasystem.com/files/thumbnail-114586c2-bf89-4d68-8f8c-a0882b4c3f65",
                    "avatarBlurhash": "yOQ*u^pGEKR6TI%LEf}[R+OXtQRQR*jEWFsnoyofR*RPWXxuofM|RPf,ofWWIoWBxat7snxZj[wdV@R+t7ofbboftRs:V@RjWCR*WU",
                    "avatarColor": null,
                    "isAdmin": false,
                    "isModerator": false,
                    "isBot": false,
                    "isCat": true,
                    "instance": {
                        "name": "Misskey.io",
                        "softwareName": "misskey",
                        "softwareVersion": "12.119.0",
                        "iconUrl": "https://misskey.io/static-assets/icons/192.png",
                        "faviconUrl": "https://s3.arkjp.net/misskey/webpublic-0c66b1ca-b8c0-4eaa-9827-47674f4a1580.png",
                        "themeColor": "#86b300"
                    },
                    "emojis": [],
                    "onlineStatus": "unknown",
                    "driveCapacityOverrideMb": null,
                    "url": "https://misskey.io/@refia",
                    "uri": "https://misskey.io/users/8df0ejllc7",
                    "createdAt": "2022-06-09T02:44:42.013Z",
                    "updatedAt": "2022-10-17T04:16:42.541Z",
                    "lastFetchedAt": "2022-10-17T04:16:42.739Z",
                    "bannerUrl": "https://misskey.pantasystem.com/files/aa419c60-cf98-4d6e-b65a-6e68d3a73903",
                    "bannerBlurhash": "yUK_a@=:01oN^%xtIV?vOGXSNLoyxat7xARjxtM|WVIoR%~nIVIUM{Myt6xs?ZN1xut8Rloft6-oRRjFabHR.oeNHt6RjM{WCoJ",
                    "bannerColor": null,
                    "isLocked": false,
                    "isSilenced": false,
                    "isSuspended": false,
                    "description": "しなちに人生をくるくるにされた\n\n\n優しい藍につつまれて",
                    "location": "mi",
                    "birthday": "2002-10-11",
                    "lang": null,
                    "fields": [
                        {
                            "name": "ついったー",
                            "value": "https://twitter.com/ref1a"
                        },
                        {
                            "name": "ついったー（おえかき）",
                            "value": "https://twitter.com/shiromamashiro"
                        },
                        {
                            "name": "ぴくしぶ",
                            "value": "https://www.pixiv.net/users/18753741"
                        },
                        {
                            "name": "いほーげーむ",
                            "value": "https://osu.ppy.sh/users/refia"
                        }
                    ],
                    "followersCount": 1,
                    "followingCount": 1,
                    "notesCount": 9,
                    "pinnedNoteIds": [
                        "8srogmvk06",
                        "8t4e4mp107",
                        "8to6vcfb08",
                        "8tmbitam0a"
                    ],
                    "pinnedNotes": [
                        {
                            "id": "8srogmvk06",
                            "createdAt": "2021-11-06T18:01:31.856Z",
                            "userId": "919zdqcdzy",
                            "user": {
                                "id": "919zdqcdzy",
                                "name": "まりふぁいあ🔥",
                                "username": "refia",
                                "host": "misskey.io",
                                "avatarUrl": "https://misskey.pantasystem.com/files/thumbnail-114586c2-bf89-4d68-8f8c-a0882b4c3f65",
                                "avatarBlurhash": "yOQ*u^pGEKR6TI%LEf}[R+OXtQRQR*jEWFsnoyofR*RPWXxuofM|RPf,ofWWIoWBxat7snxZj[wdV@R+t7ofbboftRs:V@RjWCR*WU",
                                "avatarColor": null,
                                "isCat": true,
                                "instance": {
                                    "name": "Misskey.io",
                                    "softwareName": "misskey",
                                    "softwareVersion": "12.119.0",
                                    "iconUrl": "https://misskey.io/static-assets/icons/192.png",
                                    "faviconUrl": "https://s3.arkjp.net/misskey/webpublic-0c66b1ca-b8c0-4eaa-9827-47674f4a1580.png",
                                    "themeColor": "#86b300"
                                },
                                "emojis": [],
                                "onlineStatus": "unknown",
                                "driveCapacityOverrideMb": null
                            },
                            "text": "優ちゃん",
                            "cw": null,
                            "visibility": "public",
                            "renoteCount": 0,
                            "repliesCount": 0,
                            "reactions": {
                                "❤": 1
                            },
                            "emojis": [],
                            "fileIds": [
                                "919zdrsj05"
                            ],
                            "files": [
                                {
                                    "id": "919zdrsj05",
                                    "createdAt": "2022-06-09T02:44:43.891Z",
                                    "name": "webpublic-bdfd8efe-f46b-4685-9617-5b2c4b571fe8.png",
                                    "type": "image/png",
                                    "md5": "2c9e0ea0f97ded19373b54922201ecee",
                                    "size": 1938393,
                                    "isSensitive": false,
                                    "blurhash": "yRHeR;t7~ot7_1xu-ptnj]x[a}%Loft7XAa}-QoeNHofRkbvWC%LocWVoeR*?aRj%Lf6%Ls:kD%MaekCWBs:j[WBRjWBWXazNGa{RQ",
                                    "properties": {
                                        "width": 1449,
                                        "height": 2048
                                    },
                                    "url": "https://misskey.pantasystem.com/files/c693773b-af33-458c-a6ee-f2bc7a3c1f4c",
                                    "thumbnailUrl": "https://misskey.pantasystem.com/files/thumbnail-84a9fffd-40bd-485e-96ca-c57832aa17ad",
                                    "comment": null,
                                    "folderId": null,
                                    "folder": null,
                                    "userId": null,
                                    "user": null
                                }
                            ],
                            "replyId": null,
                            "renoteId": null,
                            "uri": "https://misskey.io/notes/8srogmvk0h"
                        },
                        {
                            "id": "8t4e4mp107",
                            "createdAt": "2021-11-15T15:33:15.877Z",
                            "userId": "919zdqcdzy",
                            "user": {
                                "id": "919zdqcdzy",
                                "name": "まりふぁいあ🔥",
                                "username": "refia",
                                "host": "misskey.io",
                                "avatarUrl": "https://misskey.pantasystem.com/files/thumbnail-114586c2-bf89-4d68-8f8c-a0882b4c3f65",
                                "avatarBlurhash": "yOQ*u^pGEKR6TI%LEf}[R+OXtQRQR*jEWFsnoyofR*RPWXxuofM|RPf,ofWWIoWBxat7snxZj[wdV@R+t7ofbboftRs:V@RjWCR*WU",
                                "avatarColor": null,
                                "isCat": true,
                                "instance": {
                                    "name": "Misskey.io",
                                    "softwareName": "misskey",
                                    "softwareVersion": "12.119.0",
                                    "iconUrl": "https://misskey.io/static-assets/icons/192.png",
                                    "faviconUrl": "https://s3.arkjp.net/misskey/webpublic-0c66b1ca-b8c0-4eaa-9827-47674f4a1580.png",
                                    "themeColor": "#86b300"
                                },
                                "emojis": [],
                                "onlineStatus": "unknown",
                                "driveCapacityOverrideMb": null
                            },
                            "text": "かわいい",
                            "cw": null,
                            "visibility": "public",
                            "renoteCount": 0,
                            "repliesCount": 0,
                            "reactions": {},
                            "emojis": [],
                            "fileIds": [
                                "919zdrsf04"
                            ],
                            "files": [
                                {
                                    "id": "919zdrsf04",
                                    "createdAt": "2022-06-09T02:44:43.887Z",
                                    "name": "webpublic-ffa1024b-f7b2-4063-a6a5-cbada5b0fc0b.png",
                                    "type": "image/png",
                                    "md5": "7d653157812efbd49cda1824bb1aae76",
                                    "size": 1001892,
                                    "isSensitive": false,
                                    "blurhash": "yAODtg-;.A-=R${'$'}.8MxyXofrqV@S3oys;yFWXMcs.o}Rkt7xuWCjut6tRWYRPxbofS1RjRjogxarWn~yEX9Mxads:nNjFS9xasmM|",
                                    "properties": {
                                        "width": 1449,
                                        "height": 2048
                                    },
                                    "url": "https://misskey.pantasystem.com/files/656d99b2-595d-49a5-a60b-82420e4fe7a9",
                                    "thumbnailUrl": "https://misskey.pantasystem.com/files/thumbnail-c0518245-1b66-43d2-94bd-ec4b61d8b1c1",
                                    "comment": null,
                                    "folderId": null,
                                    "folder": null,
                                    "userId": null,
                                    "user": null
                                }
                            ],
                            "replyId": null,
                            "renoteId": null,
                            "uri": "https://misskey.io/notes/8t4e4mp18o"
                        },
                        {
                            "id": "8to6vcfb08",
                            "createdAt": "2021-11-29T12:05:28.871Z",
                            "userId": "919zdqcdzy",
                            "user": {
                                "id": "919zdqcdzy",
                                "name": "まりふぁいあ🔥",
                                "username": "refia",
                                "host": "misskey.io",
                                "avatarUrl": "https://misskey.pantasystem.com/files/thumbnail-114586c2-bf89-4d68-8f8c-a0882b4c3f65",
                                "avatarBlurhash": "yOQ*u^pGEKR6TI%LEf}[R+OXtQRQR*jEWFsnoyofR*RPWXxuofM|RPf,ofWWIoWBxat7snxZj[wdV@R+t7ofbboftRs:V@RjWCR*WU",
                                "avatarColor": null,
                                "isCat": true,
                                "instance": {
                                    "name": "Misskey.io",
                                    "softwareName": "misskey",
                                    "softwareVersion": "12.119.0",
                                    "iconUrl": "https://misskey.io/static-assets/icons/192.png",
                                    "faviconUrl": "https://s3.arkjp.net/misskey/webpublic-0c66b1ca-b8c0-4eaa-9827-47674f4a1580.png",
                                    "themeColor": "#86b300"
                                },
                                "emojis": [],
                                "onlineStatus": "unknown",
                                "driveCapacityOverrideMb": null
                            },
                            "text": "絵のおまとめクリップです\n\nhttps://misskey.io/clips/8tjexjvuqw",
                            "cw": null,
                            "visibility": "public",
                            "renoteCount": 0,
                            "repliesCount": 0,
                            "reactions": {},
                            "emojis": [],
                            "fileIds": [],
                            "files": [],
                            "replyId": null,
                            "renoteId": null,
                            "uri": "https://misskey.io/notes/8to6vcfbpu"
                        },
                        {
                            "id": "8tmbitam0a",
                            "createdAt": "2021-11-28T04:40:09.934Z",
                            "userId": "919zdqcdzy",
                            "user": {
                                "id": "919zdqcdzy",
                                "name": "まりふぁいあ🔥",
                                "username": "refia",
                                "host": "misskey.io",
                                "avatarUrl": "https://misskey.pantasystem.com/files/thumbnail-114586c2-bf89-4d68-8f8c-a0882b4c3f65",
                                "avatarBlurhash": "yOQ*u^pGEKR6TI%LEf}[R+OXtQRQR*jEWFsnoyofR*RPWXxuofM|RPf,ofWWIoWBxat7snxZj[wdV@R+t7ofbboftRs:V@RjWCR*WU",
                                "avatarColor": null,
                                "isCat": true,
                                "instance": {
                                    "name": "Misskey.io",
                                    "softwareName": "misskey",
                                    "softwareVersion": "12.119.0",
                                    "iconUrl": "https://misskey.io/static-assets/icons/192.png",
                                    "faviconUrl": "https://s3.arkjp.net/misskey/webpublic-0c66b1ca-b8c0-4eaa-9827-47674f4a1580.png",
                                    "themeColor": "#86b300"
                                },
                                "emojis": [],
                                "onlineStatus": "unknown",
                                "driveCapacityOverrideMb": null
                            },
                            "text": "にゃらべるとかわいい",
                            "cw": null,
                            "visibility": "public",
                            "renoteCount": 6,
                            "repliesCount": 0,
                            "reactions": {
                                ":otoku@mewl.me:": 1
                            },
                            "emojis": [
                                {
                                    "name": "otoku@mewl.me",
                                    "url": "https://misskey.pantasystem.com/proxy/%2Ffiles%2Fwebpublic-09416d2c-3737-4c42-b0b2-7a71e56214e7.png?url=https%3A%2F%2Fobjects.misskey.mewl.me%2Ffiles%2Fwebpublic-09416d2c-3737-4c42-b0b2-7a71e56214e7.png"
                                },
                                {
                                    "name": "kawaiii@misskey.io",
                                    "url": "https://misskey.pantasystem.com/proxy/%2Fmisskey%2Fwebpublic-59da0cdd-4071-4573-bf58-a9a07f9ba2fc.png?url=https%3A%2F%2Fs3.arkjp.net%2Fmisskey%2Fwebpublic-59da0cdd-4071-4573-bf58-a9a07f9ba2fc.png"
                                }
                            ],
                            "fileIds": [
                                "919zdsiy09"
                            ],
                            "files": [
                                {
                                    "id": "919zdsiy09",
                                    "createdAt": "2022-06-09T02:44:44.842Z",
                                    "name": "webpublic-133a9349-5006-409d-a804-2f3aa2d748b6.png",
                                    "type": "image/png",
                                    "md5": "f2ef31587030548060eb989da7685219",
                                    "size": 582934,
                                    "isSensitive": false,
                                    "blurhash": "ysRfUsg4NanPa}WCW?_Nn~s.ozj?t6jYozoJofW;jZa{jYs:W=R*nibHj[bIbbaykCj[jtafa|-;oLjuW;a{j[jskCayjsj@jZbHfQ",
                                    "properties": {
                                        "width": 2048,
                                        "height": 573
                                    },
                                    "url": "https://misskey.pantasystem.com/files/d47f4149-9613-452e-a7d9-bdd5d4508e4e",
                                    "thumbnailUrl": "https://misskey.pantasystem.com/files/thumbnail-b92645dd-f4d7-491b-8bac-53b21d81eb55",
                                    "comment": null,
                                    "folderId": null,
                                    "folder": null,
                                    "userId": null,
                                    "user": null
                                }
                            ],
                            "replyId": null,
                            "renoteId": null,
                            "uri": "https://misskey.io/notes/8tmbitamso"
                        }
                    ],
                    "pinnedPageId": null,
                    "pinnedPage": null,
                    "publicReactions": false,
                    "ffVisibility": "public",
                    "twoFactorEnabled": false,
                    "usePasswordLessLogin": false,
                    "securityKeys": false,
                    "isFollowing": true,
                    "isFollowed": true,
                    "hasPendingFollowRequestFromYou": false,
                    "hasPendingFollowRequestToYou": false,
                    "isBlocking": false,
                    "isBlocked": false,
                    "isMuted": false
                }
            }
        """.trimIndent()

        val decoder = Json {
            ignoreUnknownKeys = true
        }
        val result: ApResolveResult = decoder.decodeFromString(json)
        Assertions.assertEquals(
            "まりふぁいあ\uD83D\uDD25",
            (result as ApResolveResult.TypeUser).user.name
        )
    }
}