package net.pantasystem.milktea.api.milktea

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class InstanceInfoResponseTest {

    @Test
    fun decode() {
        val json = """[
    {
        "id": "80b426ba-be2d-4c9c-90c0-c34f9b77610c",
        "host": "misskey.io",
        "name": "Misskey.io",
        "description": "Misskey.io は、地球で生まれた分散マイクロブログSNSです。Fediverse（様々なSNSで構成される宇宙）の中に存在するため、他のSNSと相互に繋がっています。\n<br>\n暫し都会の喧騒から離れて、新しいインターネットにダイブしてみませんか。<br>\n<br>\n<a href=\"https://go.misskey.io/support\" target=\"_blank\">お問い合わせはこちら<br>https://go.misskey.io/support</a><br>\n<br>\n<br>\nPowered  by Misskey",
        "clientMaxBodyByteSize": null,
        "iconUrl": "https://s3.arkjp.net/misskey/webpublic-0c66b1ca-b8c0-4eaa-9827-47674f4a1580.png",
        "themeColor": "#86b300"
    },
    {
        "id": "43a16327-d571-44e8-8231-add1962650e2",
        "host": "misskey.pantasystem.com",
        "name": "パン太は人間",
        "description": "パン太が実験的に構築したMisskeyの鯖だよ\n落ちてたらごめんね・・\n",
        "clientMaxBodyByteSize": null,
        "iconUrl": "https://misskey.pantasystem.com/files/20ab4e6b-0ff3-4ce2-ac0d-dcb9efab0d0e",
        "themeColor": null
    },
    {
        "id": "a556cdfc-74d9-457c-bb9b-1ca3d6630f69",
        "host": "misskey.dev",
        "name": "misskey.dev",
        "description": "<h3>Misskey for Developers and any people!</h3>\n<div style=\"margin:-8px 0;\">開発者じゃなくても大丈夫！社畜でも大丈夫！開発者じゃなくても社畜じゃなくても大丈夫！なMisskey(ミスキー)インスタンスです。当インスタンスでは現在、Misskey めいv11が稼働しております。\n<span class=\"h4\" style=\"font-size:1.2em;\"><a href=\"https://misskey.dev/notes/5c79e2a0fe0a36003970239f\">How to Use</a> \n   |   <a href=\"https://misskey.dev/notes/5c79e505c9c298003288f8c8\">使い方</a>\n   |   <a href=\"https://misskey.dev/@cv_k/pages/info\">Info・情報</a>\n   \n<small style=\"opacity:40%;font-size:70%;\">powered by <a href=\"https://misskey.online\" target=\"_blank\" rel=\"noopener\" style=\"color:#ccfefd;\">MisskeyHost</a></small>",
        "clientMaxBodyByteSize": null,
        "iconUrl": "https://s3.arkjp.net/dev/16383030-9d8c-4988-813e-31cf7fa3cbd6.ico",
        "themeColor": null
    },
    {
        "id": "8e3928c5-f9f2-416d-9b1b-12ef8e73788a",
        "host": "misskey.m544.net",
        "name": "めいすきー",
        "description": "お淑やかなめいめいさん推しのためのめいめい邸ガレージなのだわ。ローカルタイムラインのない <a href=\"https://meisskey.one\"  target=\"_blank\" rel=\"noopener\">meisskey.one</a> もあるのだわ。",
        "clientMaxBodyByteSize": 100000000,
        "iconUrl": "https://misskey-drive.m544.net/files/969b8097-464a-467d-bd88-530058aa9c84.ico",
        "themeColor": null
    }
]"""

        val decoder = Json {
            ignoreUnknownKeys = true
        }
        val result: List<InstanceInfoResponse> = decoder.decodeFromString(json)
        Assertions.assertEquals(4, result.size)
        Assertions.assertEquals("めいすきー", result[3].name)
        Assertions.assertEquals("https://misskey-drive.m544.net/files/969b8097-464a-467d-bd88-530058aa9c84.ico", result[3].iconUrl)
        Assertions.assertEquals(100000000L, result[3].clientMaxBodyByteSize)
    }
}