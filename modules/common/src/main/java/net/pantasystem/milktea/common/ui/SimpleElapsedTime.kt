package net.pantasystem.milktea.common.ui

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

sealed interface TimeUnit {
    data class Year(val value: Long) : TimeUnit
    data class Month(val value: Long) : TimeUnit
    data class Day(val value: Long) : TimeUnit
    data class Hour(val value: Long) : TimeUnit
    data class Minute(val value: Long) : TimeUnit
    data class Second(val value: Long) : TimeUnit
    object Now : TimeUnit
    object Future : TimeUnit
}

object SimpleElapsedTime {

    operator fun invoke(date: Instant, now: Instant? = null): TimeUnit {
        val epoch = date.toEpochMilliseconds()
        val nowEpoch = (now ?: Clock.System.now()).toEpochMilliseconds()

        return when (val elapsedMilliTime = nowEpoch - epoch) {
            in Long.MIN_VALUE until 0 -> {
                // 0秒未満
                TimeUnit.Future
            }
            in 0 until 10 * 1000 -> {
                // 0秒以上 10秒未満
                TimeUnit.Now
            }
            in 10 * 1000 until 6 * 10 * 1000 -> {
                // 10秒以上 1分未満
                TimeUnit.Second(elapsedMilliTime / 1000)
            }
            in 6 * 10 * 1000 until 60 * (6 * 10 * 1000) -> {
                // 1分以上 60分未満
                TimeUnit.Minute(elapsedMilliTime / (6 * 10 * 1000))
            }
            in 1 * (60 * (6 * 10 * 1000)) until 24 * (60 * (6 * 10 * 1000)) -> {
                // 1時間以上 24時間未満
                TimeUnit.Hour(elapsedMilliTime / (1 * (60 * (6 * 10 * 1000))))

            }
            in 1 * (24 * (60 * (6 * 10 * 1000))) until 30 * (24 * (60 * (6 * 10 * 1000L))) -> {
                // 1日以上 30日未満
                TimeUnit.Day(elapsedMilliTime / (1 * (24 * (60 * (6 * 10 * 1000)))))

            }
            in 30 * (24 * (60 * (6 * 10 * 1000L))) until 365 * (24 * (60 * (6 * 10 * 1000L))) -> {
                // 30日以上 365未満
                TimeUnit.Month(elapsedMilliTime / (30 * (24 * (60 * (6 * 10 * 1000L)))))
            }
            else -> {
                // 365日以上
                TimeUnit.Year(elapsedMilliTime / (365 * (24 * (60 * (6 * 10 * 1000L)))))
            }
        }
    }
}