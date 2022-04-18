package net.pantasystem.milktea.data.infrastructure.account.page.db

import androidx.room.TypeConverter
import net.pantasystem.milktea.model.account.page.PageType

class TimelinePageTypeConverter{

    @TypeConverter
    fun convert(type: PageType): String{
        return type.name
    }

    @TypeConverter
    fun convert(type: String): PageType {
        return PageType.valueOf(type)
    }
}
