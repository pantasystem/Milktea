package jp.panta.misskeyandroidclient.model.users

import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.user.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class UserTest {

    @Test
    fun testGetProfileUrl() {

        val user = User.Simple(
            id = User.Id(0, "id"),
            avatarUrl = "",
            emojis = emptyList(),
            host = "",
            isBot = false,
            isCat = false,
            name = "Panta",
            userName = "Panta",
            nickname = null,
            isSameHost = true,
            instance = null,
            avatarBlurhash = null,
            badgeRoles = emptyList(),
        )

        val profileUrl = user.getProfileUrl(
            Account(
                instanceDomain = "https://example.com",
                token = "",
                remoteId = "",
                userName = "",
                instanceType = Account.InstanceType.MISSKEY
            )
        )
        assertEquals("https://example.com/@Panta", profileUrl)
    }

    @Test
    fun testGetProfileUrlWhenRemoteHost() {

        val user = User.Simple(
            id = User.Id(0, "id"),
            avatarUrl = "",
            emojis = emptyList(),
            host = "misskey.io",
            isBot = false,
            isCat = false,
            name = "Panta",
            userName = "Panta",
            nickname = null,
            isSameHost = false,
            instance = null,
            avatarBlurhash = null,
            badgeRoles = emptyList(),
        )

        val profileUrl = user.getProfileUrl(
            Account(
                instanceDomain = "https://example.com",
                token = "",
                remoteId = "",
                userName = "",
                instanceType = Account.InstanceType.MISSKEY
            )
        )
        assertEquals("https://example.com/@Panta@misskey.io", profileUrl)
    }

    @Test
    fun displayUserName_GiveSameHost() {
        val user = User.Simple(
            id = User.Id(0, "id"),
            avatarUrl = "",
            emojis = emptyList(),
            host = "misskey.io",
            isBot = false,
            isCat = false,
            name = "Panta",
            userName = "Panta",
            nickname = null,
            isSameHost = true,
            instance = null,
            avatarBlurhash = null,
            badgeRoles = emptyList(),
        )
        assertEquals("@Panta", user.displayUserName)
    }

    @Test
    fun displayUserName_GiveDifferentHost() {
        val user = User.Simple(
            id = User.Id(0, "id"),
            avatarUrl = "",
            emojis = emptyList(),
            host = "misskey.io",
            isBot = false,
            isCat = false,
            name = "Panta",
            userName = "Panta",
            nickname = null,
            isSameHost = false,
            instance = null,
            avatarBlurhash = null,
            badgeRoles = emptyList(),
        )
        assertEquals("@Panta@misskey.io", user.displayUserName)
    }

}