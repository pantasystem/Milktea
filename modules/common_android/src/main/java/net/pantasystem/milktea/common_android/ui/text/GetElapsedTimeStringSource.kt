package net.pantasystem.milktea.common_android.ui.text

import net.pantasystem.milktea.common.ui.TimeUnit
import net.pantasystem.milktea.common_android.R
import net.pantasystem.milktea.common_android.resource.StringSource

object GetElapsedTimeStringSource {
    
    operator fun invoke(timeUnit: TimeUnit): StringSource {
        return when(timeUnit) {
            is TimeUnit.Day -> StringSource(R.string.time_with_date_ago, timeUnit.value.toString())
            TimeUnit.Future -> StringSource(R.string.future)
            is TimeUnit.Hour -> StringSource(R.string.time_with_hour_ago, timeUnit.value.toString())
            is TimeUnit.Minute -> StringSource(R.string.time_with_minute_ago, timeUnit.value.toString())
            is TimeUnit.Month -> StringSource(R.string.time_with_month_ago, timeUnit.value.toString())
            TimeUnit.Now -> StringSource(R.string.now)
            is TimeUnit.Second -> StringSource(R.string.time_with_second_ago, timeUnit.value.toString())
            is TimeUnit.Year -> StringSource(R.string.time_with_year_ago, timeUnit.value.toString())
        }
    }
}