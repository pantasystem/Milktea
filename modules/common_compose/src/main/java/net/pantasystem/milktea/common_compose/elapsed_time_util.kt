package net.pantasystem.milktea.common_compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.res.stringResource
import kotlinx.datetime.Instant
import net.pantasystem.milktea.common.ui.SimpleElapsedTime
import net.pantasystem.milktea.common.ui.TimeUnit

@Composable
@Stable
fun getSimpleElapsedTime(time: Instant): String {
    return getStr(SimpleElapsedTime.invoke(time))
}

@Composable
@Stable
private fun getStr(unit: TimeUnit): String {
    return when(unit){
        is TimeUnit.Day -> stringResource(R.string.time_with_date_ago, unit.value.toString())
        TimeUnit.Future -> stringResource(R.string.future)
        is TimeUnit.Hour -> stringResource(R.string.time_with_hour_ago, unit.value.toString())
        is TimeUnit.Minute -> stringResource(R.string.time_with_minute_ago, unit.value.toString())
        is TimeUnit.Month -> stringResource(R.string.time_with_month_ago, unit.value.toString())
        TimeUnit.Now -> stringResource(R.string.now)
        is TimeUnit.Second -> stringResource(R.string.time_with_second_ago, unit.value.toString())
        is TimeUnit.Year -> stringResource(R.string.time_with_year_ago, unit.value.toString())
    }
}