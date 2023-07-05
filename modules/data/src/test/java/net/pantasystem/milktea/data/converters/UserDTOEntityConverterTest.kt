package net.pantasystem.milktea.data.converters

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import net.pantasystem.milktea.api.misskey.users.UserDTO
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.user.User
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import kotlin.time.Duration.Companion.days

class UserDTOEntityConverterTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun converter_GiveDetailedData() = runTest {
        val converter = UserDTOEntityConverter(
            mock() {
                onBlocking {
                    getAndConvertToMap(any())
                } doReturn mapOf()
            },
            mock() {
                onBlocking {
                    findIn(any())
                } doReturn Result.success(emptyList())
            },
            mock() {
                onBlocking {
                    findBySourceUrls(any())
                } doReturn emptyList()
            }
        )

        val userDTO = UserDTO(
            id = "test-id",
            userName = "test-user-name1",
            name = "name-1",
            host = "misskey.io",
            description = "hogehoge",
            followersCount = 10,
            followingCount = 20,
            hostLower = null,
            notesCount = 128,
            email = null,
            isBot = false,
            isCat = true,
            pinnedNoteIds = listOf(),
            pinnedNotes = listOf(),
            twoFactorEnabled = null,
            isAdmin = null,
            avatarUrl = "avatarUrl",
            bannerUrl = "bannerUrl",
            rawEmojis = null,
            isFollowing = true,
            isFollowed = false,
            isBlocking = false,
            isMuted = false,
            url = "https://url",
            hasPendingFollowRequestFromYou = false,
            hasPendingFollowRequestToYou = false,
            isLocked = false,
            instance = null,
            fields = listOf(),
            birthday = Clock.System.now().toLocalDateTime(TimeZone.UTC).date,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now().plus(1.days),
            publicReactions = null,
            avatarBlurhash = "aowfjioa0392"
        )
        val account = Account(
            remoteId = "test-id",
            instanceDomain = "https://misskey.pantasystem.com",
            userName = "Panta",
            instanceType = Account.InstanceType.MISSKEY,
            token = "",
        ).copy(accountId = 1L)

        val result = converter.convert(account, userDTO, true) as User.Detail
        Assertions.assertEquals(userDTO.userName, result.userName)
        Assertions.assertEquals(userDTO.name, result.name)
        Assertions.assertEquals(userDTO.host, result.host)
        Assertions.assertEquals(userDTO.description, result.info.description)
        Assertions.assertEquals(userDTO.hostLower, result.info.hostLower)
        Assertions.assertEquals(userDTO.followersCount, result.info.followersCount)
        Assertions.assertEquals(userDTO.followingCount, result.info.followingCount)
        Assertions.assertEquals(userDTO.notesCount, result.info.notesCount)
        Assertions.assertEquals(userDTO.isBot, result.isBot)
        Assertions.assertEquals(userDTO.isCat, result.isCat)
        Assertions.assertEquals(userDTO.isFollowed, result.related?.isFollower)
        Assertions.assertEquals(userDTO.isFollowing, result.related?.isFollowing)
        Assertions.assertEquals(userDTO.isBlocking, result.related?.isBlocking)
        Assertions.assertEquals(userDTO.isMuted, result.related?.isMuting)
        Assertions.assertEquals(userDTO.url, result.info.url)
        Assertions.assertEquals(userDTO.hasPendingFollowRequestFromYou, result.related?.hasPendingFollowRequestFromYou)
        Assertions.assertEquals(userDTO.hasPendingFollowRequestToYou, result.related?.hasPendingFollowRequestToYou)
        Assertions.assertEquals(userDTO.avatarUrl, result.avatarUrl)
        Assertions.assertEquals(userDTO.bannerUrl, result.info.bannerUrl)
        Assertions.assertEquals(userDTO.avatarBlurhash, result.avatarBlurhash)
    }

    @Test
    @OptIn(ExperimentalCoroutinesApi::class)
    fun convert_GiveSimpleUser() = runTest {
        val converter = UserDTOEntityConverter(
            mock() {
                onBlocking {
                    getAndConvertToMap(any())
                } doReturn mapOf()
            },
            mock() {
                onBlocking {
                    findIn(any())
                } doReturn Result.success(emptyList())
            },
            mock() {
                onBlocking {
                    findBySourceUrls(any())
                } doReturn emptyList()
            }
        )

        val userDTO = UserDTO(
            id = "test-id",
            userName = "test-user-name2",
            name = "name-2",
            host = "misskey.io",
            description = "hogehogewefaw",
            followersCount = 10,
            followingCount = 20,
            hostLower = null,
            notesCount = 128,
            email = null,
            isBot = false,
            isCat = true,
            pinnedNoteIds = listOf(),
            pinnedNotes = listOf(),
            twoFactorEnabled = null,
            isAdmin = null,
            avatarUrl = "avatarUrl",
            bannerUrl = "bannerUrl",
            rawEmojis = null,
            isFollowing = true,
            isFollowed = false,
            isBlocking = false,
            isMuted = false,
            url = "https://url",
            hasPendingFollowRequestFromYou = false,
            hasPendingFollowRequestToYou = false,
            isLocked = false,
            instance = null,
            fields = listOf(),
            birthday = Clock.System.now().toLocalDateTime(TimeZone.UTC).date,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now().plus(1.days),
            publicReactions = null,
            avatarBlurhash = "aowfjioa0392"
        )
        val account = Account(
            remoteId = "test-id",
            instanceDomain = "https://misskey.pantasystem.com",
            userName = "Panta",
            instanceType = Account.InstanceType.MISSKEY,
            token = "",
        ).copy(accountId = 1L)

        val result = converter.convert(account, userDTO, true)
        Assertions.assertEquals(userDTO.userName, result.userName)
        Assertions.assertEquals(userDTO.name, result.name)
        Assertions.assertEquals(userDTO.host, result.host)

        Assertions.assertEquals(userDTO.isBot, result.isBot)
        Assertions.assertEquals(userDTO.isCat, result.isCat)

        Assertions.assertEquals(userDTO.avatarUrl, result.avatarUrl)
        Assertions.assertEquals(userDTO.avatarBlurhash, result.avatarBlurhash)
    }
}