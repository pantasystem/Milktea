package net.pantasystem.milktea.data.infrastructure.user.renote.mute.delegate

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.RenoteMuteCache
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.db.RenoteMuteDao
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.db.RenoteMuteRecord
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.renote.mute.RenoteMute
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class FindRenoteMuteAndUpdateMemCacheDelegateTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun whenExists() = runTest {
        val now = Clock.System.now()
        val cache = RenoteMuteCache()

        val dao: RenoteMuteDao = object : RenoteMuteDao {
            override suspend fun insert(renoteMuteRecord: RenoteMuteRecord): Long = 1L
            override suspend fun insertAll(records: List<RenoteMuteRecord>): List<Long> = emptyList()
            override suspend fun update(renoteMuteRecord: RenoteMuteRecord) = Unit
            override suspend fun findByAccount(accountId: Long): List<RenoteMuteRecord> = emptyList()
            override suspend fun findByUser(accountId: Long, userId: String): RenoteMuteRecord = RenoteMuteRecord(0L, "user-1", now, null)
            override fun observeByUser(accountId: Long, userId: String): Flow<RenoteMuteRecord?> = emptyFlow()
            override suspend fun delete(accountId: Long, userId: String) = Unit
            override suspend fun deleteBy(accountId: Long) = Unit
            override fun observeBy(accountId: Long): Flow<List<RenoteMuteRecord>> = emptyFlow()
            override suspend fun findByUnPushed(accountId: Long): List<RenoteMuteRecord> = emptyList()
        }
        val delegate = FindRenoteMuteAndUpdateMemCacheDelegateImpl(
            renoteMuteDao = dao,
            cache = cache,
            Dispatchers.Default
        )
        val result = delegate.invoke(User.Id(0L, "user-1")).getOrThrow()
        Assertions.assertEquals(
            RenoteMute(
                User.Id(0L, "user-1"),
                now,
                null,
            ),
            result
        )
        Assertions.assertTrue(cache.exists(User.Id(0L, "user-1")))
        Assertions.assertFalse(cache.isNotFound(User.Id(0L, "user-1")))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun whenNotExists() = runTest {
        val cache = RenoteMuteCache()
        val dao: RenoteMuteDao = object : RenoteMuteDao {
            override suspend fun insert(renoteMuteRecord: RenoteMuteRecord): Long = 1L
            override suspend fun insertAll(records: List<RenoteMuteRecord>): List<Long> = emptyList()
            override suspend fun update(renoteMuteRecord: RenoteMuteRecord) = Unit
            override suspend fun findByAccount(accountId: Long): List<RenoteMuteRecord> = emptyList()
            override suspend fun findByUser(accountId: Long, userId: String): RenoteMuteRecord? = null
            override fun observeByUser(accountId: Long, userId: String): Flow<RenoteMuteRecord?> = emptyFlow()
            override suspend fun delete(accountId: Long, userId: String) = Unit
            override suspend fun deleteBy(accountId: Long) = Unit
            override fun observeBy(accountId: Long): Flow<List<RenoteMuteRecord>> = emptyFlow()
            override suspend fun findByUnPushed(accountId: Long): List<RenoteMuteRecord> = emptyList()
        }
        val delegate = FindRenoteMuteAndUpdateMemCacheDelegateImpl(
            renoteMuteDao = dao,
            cache = cache,
            Dispatchers.Default
        )
        val result = delegate.invoke(User.Id(0L, "user-1"))
        Assertions.assertTrue(result.isFailure)
        Assertions.assertFalse(cache.exists(User.Id(0L, "user-1")))
        Assertions.assertTrue(cache.isNotFound(User.Id(0L, "user-1")))

    }
}