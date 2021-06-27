package jp.panta.misskeyandroidclient.view.text

import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.util.date.toCurrentLocaleDate
import jp.panta.misskeyandroidclient.view.SimpleElapsedTime
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import java.text.SimpleDateFormat
import java.util.*

object DateFormatHelper {

    @BindingAdapter("dateOnly")
    @JvmStatic
    fun TextView.setDateOnly(dateOnly: Date?){
        val date = dateOnly?: Date()
        val sdf = SimpleDateFormat("yyyy/M/d", Locale.getDefault())
        this.text = sdf.format(date)
    }

    @BindingAdapter("timeOnly")
    @JvmStatic
    fun TextView.setTimeOnly(timeOnly: Date?){
        val date = timeOnly?: Date()
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        this.text = sdf.format(date)
    }

    @BindingAdapter("elapsedTime")
    @JvmStatic
    fun TextView.setElapsedTime(elapsedTime: Instant?){

        val simpleElapsedTime = SimpleElapsedTime{
            when(it){
                SimpleElapsedTime.TimeUnit.YEAR -> context.getString(R.string.year_ago)
                SimpleElapsedTime.TimeUnit.MONTH -> context.getString(R.string.month_ago)
                SimpleElapsedTime.TimeUnit.DATE -> context.getString(R.string.date_ago)
                SimpleElapsedTime.TimeUnit.HOUR -> context.getString(R.string.hour_ago)
                SimpleElapsedTime.TimeUnit.MINUTE -> context.getString(R.string.minute_ago)
                SimpleElapsedTime.TimeUnit.SECOND -> context.getString(R.string.second_ago)
                SimpleElapsedTime.TimeUnit.NOW -> context.getString(R.string.now)
                SimpleElapsedTime.TimeUnit.FUTURE -> context.getString(R.string.future)
            }
        }
        this.text = simpleElapsedTime.format(elapsedTime ?: Clock.System.now())
    }

    @BindingAdapter("createdAt")
    @JvmStatic
    fun TextView.setCreatedAt(createdAt: Instant?){
        val date = createdAt?: Clock.System.now()
        val javaDate = Date(date.toEpochMilliseconds())
        this.text = SimpleDateFormat.getDateTimeInstance().format(javaDate)
    }
}