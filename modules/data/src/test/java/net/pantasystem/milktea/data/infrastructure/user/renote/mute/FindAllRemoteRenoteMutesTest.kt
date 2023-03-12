package net.pantasystem.milktea.data.infrastructure.user.renote.mute

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import net.pantasystem.milktea.api.misskey.users.UserDTO
import net.pantasystem.milktea.api.misskey.users.renote.mute.RenoteMuteDTO
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.user.User
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class FindAllRemoteRenoteMutesTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun give3Page() = runTest {
        val page1 = (0 until 11).map {
            RenoteMuteDTO(
                id = "1+$it",
                createdAt = Clock.System.now(),
                mutee = UserDTO("", ""),
                muteeId = "test"
            )
        }
        Assertions.assertEquals(11, page1.size)
        val page2 = (0 until 11).map {
            RenoteMuteDTO(
                id = "2+$it",
                createdAt = Clock.System.now(),
                mutee = UserDTO("", ""),
                muteeId = "test"
            )
        }
        Assertions.assertEquals(11, page2.size)
        val page3 = (0 until 11).map {
            RenoteMuteDTO(
                id = "3+$it",
                createdAt = Clock.System.now(),
                mutee = UserDTO("", ""),
                muteeId = "test"
            )
        }
        Assertions.assertEquals(11, page3.size)
        val pageMap = mapOf(
            page1.last().id to page2,
            page2.last().id to page3,
        )
        val target = FindAllRemoteRenoteMutes(
            account = Account(
                remoteId = "",
                instanceDomain = "",
                userName = "",
                instanceType = Account.InstanceType.MISSKEY,
                token = ""
            ),
            renoteMuteApiAdapter = object : RenoteMuteApiAdapter {
                override suspend fun create(userId: User.Id) = Unit
                override suspend fun delete(userId: User.Id) = Unit
                override suspend fun findBy(
                    accountId: Long,
                    sinceId: String?,
                    untilId: String?
                ): List<RenoteMuteDTO> {
                    return if (untilId == null) {
                        page1
                    } else {
                        pageMap[untilId] ?: emptyList()
                    }
                }
            }
        )
        val actual = target.invoke()
        val expect = page1 + page2 + page3

        Assertions.assertEquals(expect, actual)
    }
}