package jp.panta.misskeyandroidclient.model.users

import jp.panta.misskeyandroidclient.model.account.Account
import junit.framework.TestCase

class UserTest : TestCase() {

    fun testGetProfileUrl() {

        val user = User.Simple(
            id = User.Id(0, "id"),
            avatarUrl = "",
            emojis = emptyList(),
            host = null,
            isBot = false,
            isCat = false,
            name = "Panta",
            userName = "Panta"
        )

        val profileUrl = user.getProfileUrl(
            Account(
                instanceDomain = "https://example.com",
                encryptedToken = "",
                remoteId = "",
                userName = ""
            ))
        assertEquals("https://example.com/@Panta", profileUrl)
    }

    fun testGetProfileUrlWhenRemoteHost() {

        val user = User.Simple(
            id = User.Id(0, "id"),
            avatarUrl = "",
            emojis = emptyList(),
            host = "misskey.io",
            isBot = false,
            isCat = false,
            name = "Panta",
            userName = "Panta"
        )

        val profileUrl = user.getProfileUrl(Account(
            instanceDomain = "https://example.com",
            encryptedToken = "",
            remoteId = "",
            userName = ""
        ))
        assertEquals("https://example.com/@Panta@misskey.io", profileUrl)
    }
}