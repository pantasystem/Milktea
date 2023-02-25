package net.pantasystem.milktea.common.ui

import kotlinx.datetime.Clock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds


class SimpleElapsedTimeTest {

    @Test
    fun giveMinusSecondsAgoReturnsFuture() {
        val now = Clock.System.now()

        val result = SimpleElapsedTime(now.minus((-1).seconds), now)
        Assertions.assertEquals(TimeUnit.Future, result)
    }

    @Test
    fun give5SecondsAgoReturnsNow() {
        val now = Clock.System.now()

        val result = SimpleElapsedTime(now.minus(5.seconds), now)
        Assertions.assertEquals(TimeUnit.Now, result)
    }

    @Test
    fun give9SecondsAgoReturnsNow() {
        val now = Clock.System.now()

        val result = SimpleElapsedTime(now.minus(9.seconds), now)
        Assertions.assertEquals(TimeUnit.Now, result)
    }

    @Test
    fun give10SecondsAgoReturns10Seconds() {
        val now = Clock.System.now()

        val result = SimpleElapsedTime(now.minus(10.seconds), now)
        Assertions.assertEquals(TimeUnit.Second(10), result)
    }

    @Test
    fun give59SecondsAgoReturns59Seconds() {
        val now = Clock.System.now()

        val result = SimpleElapsedTime(now.minus(59.seconds), now)
        Assertions.assertEquals(TimeUnit.Second(59), result)
    }

    @Test
    fun give60SecondsAgoReturns1Minutes() {
        val now = Clock.System.now()

        val result = SimpleElapsedTime(now.minus(60.seconds), now)
        Assertions.assertEquals(TimeUnit.Minute(1), result)
    }

    @Test
    fun give59MinutesAgoReturns59Minutes() {
        val now = Clock.System.now()

        val result = SimpleElapsedTime(now.minus(59.minutes), now)
        Assertions.assertEquals(TimeUnit.Minute(59), result)
    }

    @Test
    fun give60MinutesAgoReturns1Hours() {
        val now = Clock.System.now()

        val result = SimpleElapsedTime(now.minus(60.minutes), now)
        Assertions.assertEquals(TimeUnit.Hour(1), result)
    }

    @Test
    fun give23HoursAgoReturns23Hours() {
        val now = Clock.System.now()

        val result = SimpleElapsedTime(now.minus(23.hours), now)
        Assertions.assertEquals(TimeUnit.Hour(23), result)
    }

    @Test
    fun give24HoursAgoReturns1Days() {
        val now = Clock.System.now()

        val result = SimpleElapsedTime(now.minus(24.hours), now)
        Assertions.assertEquals(TimeUnit.Day(1), result)
    }

    @Test
    fun give29DaysReturns29Days() {
        val now = Clock.System.now()

        val result = SimpleElapsedTime(now.minus(29.days), now)
        Assertions.assertEquals(TimeUnit.Day(29), result)
    }

    @Test
    fun give30DaysReturns1Month() {
        val now = Clock.System.now()

        val result = SimpleElapsedTime(now.minus(30.days), now)
        Assertions.assertEquals(TimeUnit.Month(1), result)
    }


}