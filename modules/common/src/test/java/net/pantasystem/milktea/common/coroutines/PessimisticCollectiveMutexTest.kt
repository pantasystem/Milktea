package net.pantasystem.milktea.common.coroutines

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class PessimisticCollectiveMutexTest {
    @Test
    fun withLock() = runTest {
        val inserter = PessimisticCollectiveMutex<String>()
        inserter.withLock("host") {
            Assertions.assertNotNull(inserter.locks["host"])
            Assertions.assertEquals(inserter.locks["host"]?.isLocked, true)
        }
        Assertions.assertNotNull(inserter.locks["host"])
        Assertions.assertEquals(inserter.locks["host"]?.isLocked, false)
    }
}