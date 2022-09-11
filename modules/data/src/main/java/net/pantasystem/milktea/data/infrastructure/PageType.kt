package net.pantasystem.milktea.data.infrastructure

import androidx.room.TypeConverter

enum class PageType(val defaultName: String){
    HOME("Home"),
    LOCAL("Local"),
    SOCIAL("Social"),
    GLOBAL("Global"),
    SEARCH("Search"),
    SEARCH_HASH("Hash"),
    USER("User"),
    FAVORITE("Favorite"),
    FEATURED("Featured"),
    DETAIL("Detail"),
    USER_LIST("List"),
    MENTION("Mention"),
    ANTENNA("Antenna"),
    NOTIFICATION("Notification")
    //USER_PINは別
}

@Deprecated("model.account.pages.Pageへ移行")
class PageTypeConverter{

    @TypeConverter
    fun convert(type: PageType): String{
        return type.name
    }

    @TypeConverter
    fun convert(type: String): PageType{
        return PageType.valueOf(type)
    }
}