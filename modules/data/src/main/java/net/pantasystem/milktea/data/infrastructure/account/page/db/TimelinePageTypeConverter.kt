package net.pantasystem.milktea.data.infrastructure.account.page.db

import androidx.room.TypeConverter
import net.pantasystem.milktea.model.account.page.PageType

class TimelinePageTypeConverter{

    @TypeConverter
    fun convert(type: PageType): String{
        return type.label
    }

    @TypeConverter
    fun convert(type: String): PageType {
        return PageType.values().first {
            it.label == type
        }
    }
}
