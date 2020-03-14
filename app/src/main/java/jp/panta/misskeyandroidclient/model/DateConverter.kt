package jp.panta.misskeyandroidclient.model

import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.text.SimpleDateFormat
import java.util.*

@TypeConverters
class DateConverter{

    companion object{
        private val smf = SimpleDateFormat.getInstance()
    }

    @TypeConverter
    fun toDate(formattedDate: String): Date{
        return smf.parse(formattedDate)
    }

    @TypeConverter
    fun fromDate(date: Date): String{
        return smf.format(date)
    }
}