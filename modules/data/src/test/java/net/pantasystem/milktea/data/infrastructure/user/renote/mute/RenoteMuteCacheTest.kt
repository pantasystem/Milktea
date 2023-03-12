package net.pantasystem.milktea.data.infrastructure.user.renote.mute

import kotlinx.datetime.Clock
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.renote.mute.RenoteMute
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class RenoteMuteCacheTest {

    @Test
    fun add() {
        val cache = RenoteMuteCache()
        cache.add(RenoteMute(User.Id(0L, "user1"), Clock.System.now(), null))
        Assertions.assertTrue(cache.exists(User.Id(0L, "user1")))
        Assertions.assertFalse(cache.isNotFound(User.Id(0L, "user1")))
    }

    @Test
    fun remove() {
        val cache = RenoteMuteCache()
        cache.remove(User.Id(0L, "user1"))
        Assertions.assertFalse(cache.exists(User.Id(0L, "user1")))
        Assertions.assertTrue(cache.isNotFound(User.Id(0L, "user1")))
    }

    @Test
    fun addAndRemove() {
        val cache = RenoteMuteCache()
        cache.add(RenoteMute(User.Id(0L, "user1"), Clock.System.now(), null))
        cache.remove(User.Id(0L, "user1"))

        Assertions.assertFalse(cache.exists(User.Id(0L, "user1")))
        Assertions.assertTrue(cache.isNotFound(User.Id(0L, "user1")))
    }

    @Test
    fun doNothing() {
        val cache = RenoteMuteCache()
        Assertions.assertFalse(cache.isNotFound(User.Id(0L, "user1")))
        Assertions.assertFalse(cache.exists(User.Id(0L, "user1")))
    }

    @Test
    fun addAll() {
        val cache = RenoteMuteCache()
        val d1 = RenoteMute(User.Id(0L, "user1"), Clock.System.now(), null)
        val d2 = RenoteMute(User.Id(0L, "user2"), Clock.System.now(), null)
        val d3 = RenoteMute(User.Id(0L, "user3"), Clock.System.now(), null)
        val d4 = RenoteMute(User.Id(0L, "user4"), Clock.System.now(), null)
        val data = listOf(d1, d2, d3, d4)
        cache.addAll(data)
        Assertions.assertTrue(cache.exists(d1.userId))
        Assertions.assertTrue(cache.exists(d2.userId))
        Assertions.assertTrue(cache.exists(d3.userId))
        Assertions.assertTrue(cache.exists(d4.userId))

        Assertions.assertFalse(cache.isNotFound(d1.userId))
        Assertions.assertFalse(cache.isNotFound(d2.userId))
        Assertions.assertFalse(cache.isNotFound(d3.userId))
        Assertions.assertFalse(cache.isNotFound(d4.userId))
    }

    @Test
    fun clearByAccount() {
        val cache = RenoteMuteCache()
        val d1 = RenoteMute(User.Id(0L, "user1"), Clock.System.now(), null)
        val d2 = RenoteMute(User.Id(0L, "user2"), Clock.System.now(), null)
        val d3 = RenoteMute(User.Id(1L, "user3"), Clock.System.now(), null)
        val d4 = RenoteMute(User.Id(1L, "user4"), Clock.System.now(), null)
        val data = listOf(d1, d2, d3, d4)
        cache.addAll(data)
        cache.clearBy(1L)
        Assertions.assertTrue(cache.exists(d1.userId))
        Assertions.assertTrue(cache.exists(d2.userId))
        Assertions.assertFalse(cache.exists(d3.userId))
        Assertions.assertFalse(cache.exists(d4.userId))

        Assertions.assertFalse(cache.isNotFound(d1.userId))
        Assertions.assertFalse(cache.isNotFound(d2.userId))
        Assertions.assertTrue(cache.isNotFound(d3.userId))
        Assertions.assertTrue(cache.isNotFound(d4.userId))
    }
}