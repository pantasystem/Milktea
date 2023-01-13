package net.pantasystem.milktea.common.ui

import kotlinx.datetime.Clock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds


class SimpleElapsedTimeTest {

    @Test
    fun giveLessThan5SecondsReturnsNow() {
        val now = Clock.System.now()

        val result = SimpleElapsedTime(now.minus(6.seconds))
        Assertions.assertEquals(TimeUnit.Now, result)
    }
}