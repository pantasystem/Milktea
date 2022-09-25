package net.pantasystem.milktea.data.infrastructure

import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import java.text.SimpleDateFormat
import java.util.*

@TypeConverters
class DateConverter{

    companion object{
        private val smf = SimpleDateFormat.getInstance()
    }

    @TypeConverter
    fun toDate(formattedDate: String?): Date? {
        formattedDate ?: return null
        return smf.parse(formattedDate)
    }

    @TypeConverter
    fun fromDate(date: Date?): String? {
        date?: return null
        return smf.format(date)
    }
}

@TypeConverters
object InstantConverter {

    @TypeConverter
    fun toInstant(iso8601DateTime: String?): Instant? {
        return iso8601DateTime?.let {
            Instant.parse(iso8601DateTime)
        }
    }

    @TypeConverter
    fun fromInstant(instant: Instant?): String? {
        return instant?.toString()
    }
}

@TypeConverters
object LocalDateConverter {
    @TypeConverter
    fun toLocalDate(str: String?): LocalDate? {
        return str?.let {
            LocalDate.parse(it)
        }
    }

    @TypeConverter
    fun fromLocalDate(localDate: LocalDate?): String? {
        return localDate?.toString()
    }
}