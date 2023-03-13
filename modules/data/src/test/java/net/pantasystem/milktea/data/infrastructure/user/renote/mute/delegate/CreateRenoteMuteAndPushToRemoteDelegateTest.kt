package net.pantasystem.milktea.data.infrastructure.user.renote.mute.delegate

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import net.pantasystem.milktea.api.misskey.users.renote.mute.RenoteMuteDTO
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.IsSupportRenoteMuteInstance
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.RenoteMuteApiAdapter
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.db.RenoteMuteDao
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.db.RenoteMuteRecord
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.renote.mute.RenoteMute
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class CreateRenoteMuteAndPushToRemoteDelegateTest {

    // キャッシュに存在しない
    // APIがRenoteMuteをサポートしている場合
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun notExistsCacheAndSupportRenoteMute() = runTest {

        val expect = RenoteMute(
            User.Id(0L, "user-1"),
            createdAt = Clock.System.now(),
            postedAt = null,
        )

        var actualByUpdated: RenoteMuteRecord? = null
        var actualByInserted: RenoteMuteRecord? = null
        val dao: RenoteMuteDao = object : RenoteMuteDao {
            override suspend fun insert(renoteMuteRecord: RenoteMuteRecord): Long {
                actualByInserted = renoteMuteRecord
                return 1
            }

            override suspend fun update(renoteMuteRecord: RenoteMuteRecord) {
                actualByUpdated = renoteMuteRecord
            }

            override suspend fun insertAll(records: List<RenoteMuteRecord>): List<Long> = emptyList()
            override suspend fun findByAccount(accountId: Long): List<RenoteMuteRecord> = emptyList()
            override suspend fun findByUser(accountId: Long, userId: String): RenoteMuteRecord? = null
            override fun observeByUser(accountId: Long, userId: String): Flow<RenoteMuteRecord?> = emptyFlow()
            override suspend fun delete(accountId: Long, userId: String) = Unit
            override suspend fun deleteBy(accountId: Long) = Unit
            override fun observeBy(accountId: Long): Flow<List<RenoteMuteRecord>> = emptyFlow()
            override suspend fun findByUnPushed(accountId: Long): List<RenoteMuteRecord> = emptyList()

        }



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

        Assertions.assertNotNull(actualByInserted)
        Assertions.assertNotNull(actualByUpdated)
        Assertions.assertNotNull(actualByUpdated?.postedAt)
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