package net.pantasystem.milktea.common_android.ui.text

import android.widget.TextView
import androidx.databinding.BindingAdapter
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import net.pantasystem.milktea.common.ui.SimpleElapsedTime
import java.text.SimpleDateFormat
import java.util.*

object DateFormatHelper {

    @BindingAdapter("dateOnly")
    @JvmStatic
    fun TextView.setDateOnly(dateOnly: Date?) {
        val date = dateOnly ?: Date()
        val sdf = SimpleDateFormat("yyyy/M/d", Locale.getDefault())
        this.text = sdf.format(date)
    }

    @BindingAdapter("timeOnly")
    @JvmStatic
    fun TextView.setTimeOnly(timeOnly: Date?) {
        val date = timeOnly ?: Date()
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        this.text = sdf.format(date)
    }

    @BindingAdapter("elapsedTime")
    @JvmStatic
    fun TextView.setElapsedTime(elapsedTime: Instant?) {

        this.text = GetElapsedTimeStringSource(
            SimpleElapsedTime.invoke(
                elapsedTime ?: Clock.System.now()
            )
        ).getString(context)
    }

    @BindingAdapter("createdAt")
    @JvmStatic
    fun TextView.setCreatedAt(createdAt: Instant?) {
        val date = createdAt ?: Clock.System.now()
        val javaDate = Date(date.toEpochMilliseconds())
        this.text = SimpleDateFormat.getDateTimeInstance().format(javaDate)
    }
}