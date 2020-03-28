package jp.panta.misskeyandroidclient.view.text

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.view.SimpleElapsedTime
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
    fun TextView.setElapsedTime(elapsedTime: Date?){
        val date = elapsedTime?: Date()
        val simpleElapsedTime = SimpleElapsedTime{
            when(it){
                SimpleElapsedTime.TimeUnit.YEAR -> context.getString(R.string.year_ago)
                SimpleElapsedTime.TimeUnit.MONTH -> context.getString(R.string.month_ago)
                SimpleElapsedTime.TimeUnit.DATE -> context.getString(R.string.date_ago)
                SimpleElapsedTime.TimeUnit.HOUR -> context.getString(R.string.minute_ago)
                SimpleElapsedTime.TimeUnit.MINUTE -> context.getString(R.string.minute_ago)
                SimpleElapsedTime.TimeUnit.SECOND -> context.getString(R.string.second_ago)
                SimpleElapsedTime.TimeUnit.NOW -> context.getString(R.string.now)
                SimpleElapsedTime.TimeUnit.FUTURE -> context.getString(R.string.future)
            }
        }
        this.text = simpleElapsedTime.format(date)
    }
}