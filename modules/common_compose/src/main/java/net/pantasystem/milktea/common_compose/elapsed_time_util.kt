package net.pantasystem.milktea.common_compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.res.stringResource
import kotlinx.datetime.Instant
import net.pantasystem.milktea.common.ui.SimpleElapsedTime

@Composable
@Stable
fun getSimpleElapsedTime(time: Instant): String {

    val map = SimpleElapsedTime.TimeUnit.values().associateWith {
        getStr(unit = it)
    }
    return SimpleElapsedTime {
        map[it]!!
    }.invoke(time)
}

@Composable
@Stable
private fun getStr(unit: SimpleElapsedTime.TimeUnit): String {
    return when(unit){
        SimpleElapsedTime.TimeUnit.YEAR -> stringResource(R.string.year_ago)
        SimpleElapsedTime.TimeUnit.MONTH -> stringResource(R.string.month_ago)
        SimpleElapsedTime.TimeUnit.DATE -> stringResource(R.string.date_ago)
        SimpleElapsedTime.TimeUnit.HOUR -> stringResource(R.string.hour_ago)
        SimpleElapsedTime.TimeUnit.MINUTE -> stringResource(R.string.minute_ago)
        SimpleElapsedTime.TimeUnit.SECOND -> stringResource(R.string.second_ago)
        SimpleElapsedTime.TimeUnit.NOW -> stringResource(R.string.now)
        SimpleElapsedTime.TimeUnit.FUTURE -> stringResource(R.string.future)
    }
}