package net.pantasystem.milktea.data.infrastructure.user.renote.mute.delegate

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.RenoteMuteCache
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.db.RenoteMuteRecord
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.renote.mute.RenoteMute
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class FindRenoteMuteAndUpdateMemCacheDelegateTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun whenExists() = runTest {
        val now = Clock.System.now()
        val cache = RenoteMuteCache()
        val delegate = FindRenoteMuteAndUpdateMemCacheDelegateImpl(
            renoteMuteDao = mock() {
                onBlocking {
                    findByUser(any(), any())
                } doReturn RenoteMuteRecord(0L, "user-1", now, null)
            },
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
        val delegate = FindRenoteMuteAndUpdateMemCacheDelegateImpl(
            renoteMuteDao = mock() {
                onBlocking {
                    findByUser(any(), any())
                } doReturn null
            },
            cache = cache,
            Dispatchers.Default
        )
        val result = delegate.invoke(User.Id(0L, "user-1"))
        Assertions.assertTrue(result.isFailure)
        Assertions.assertFalse(cache.exists(User.Id(0L, "user-1")))
        Assertions.assertTrue(cache.isNotFound(User.Id(0L, "user-1")))
    }
}