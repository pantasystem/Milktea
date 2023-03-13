package net.pantasystem.milktea.data.infrastructure.user.renote.mute.delegate

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import net.pantasystem.milktea.api.misskey.users.renote.mute.RenoteMuteDTO
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.IsSupportRenoteMuteInstance
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.RenoteMuteApiAdapter
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.db.RenoteMuteDao
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.renote.mute.RenoteMute
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class CreateRenoteMuteAndPushToRemoteDelegateTest {

    // キャッシュに存在しない
    // APIがRenoteMuteをサポートしている場合
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun notExistsCacheAndSupportRenoteMute() = runTest {

        val dao: RenoteMuteDao = mock() {
            onBlocking {
                insert(any())
            } doReturn 1

            onBlocking {
                update(any())
            }
        }

        val expect = RenoteMute(
            User.Id(0L, "user-1"),
            createdAt = Clock.System.now(),
            postedAt = null,
        )

        val delegate = CreateRenoteMuteAndPushToRemoteDelegate(
            getAccount = {
                Account(
                    remoteId = "",
                    instanceDomain = "",
                    userName = "",
                    instanceType = Account.InstanceType.MISSKEY,
                    token = ""
                )
            },
            renoteMuteApiAdapter = object : RenoteMuteApiAdapter {
                override suspend fun create(userId: User.Id) = Unit
                override suspend fun delete(userId: User.Id) = Unit
                override suspend fun findBy(
                    accountId: Long,
                    sinceId: String?,
                    untilId: String?,
                ): List<RenoteMuteDTO> = emptyList()
            },
            findRenoteMuteAndUpdateMemCache = object : FindRenoteMuteAndUpdateMemCacheDelegate {
                var isFirst = true
                override suspend fun invoke(userId: User.Id): Result<RenoteMute> {
                    if (isFirst) {
                        isFirst = false
                        return Result.failure(NoSuchElementException())
                    }
                    return Result.success(
                        expect
                    )
                }
            },
            isSupportRenoteMuteInstance = object : IsSupportRenoteMuteInstance {
                override suspend fun invoke(accountId: Long): Boolean {
                    return true
                }
            },
            renoteMuteDao = dao,
            coroutineDispatcher = Dispatchers.Default
        )

        val result = delegate.invoke(User.Id(0L, "user-1"))
            .getOrThrow()

        Assertions.assertEquals(
            expect,
            result
        )


    }


    // キャッシュに存在しない
    // APIがRenoteMuteをサポートしていない場合

    // キャッシュに存在する
    // まだ未送信である
    // APIがRenoteMuteをサポートしている場合

    // キャッシュに存在する
    // まだ未送信である
    // APIがRenoteMuteをサポートしていない場合

    // キャッシュに存在する
    // すでに送信済みの場合
}