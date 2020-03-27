package jp.panta.misskeyandroidclient.view.text

import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
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
}