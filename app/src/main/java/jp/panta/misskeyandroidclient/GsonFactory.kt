package jp.panta.misskeyandroidclient

import com.google.gson.*
import java.lang.reflect.Type
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object GsonFactory {
    private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .create()


    fun create(): Gson{
        /*val gsonBuilder = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")*/
        return gson
    }

    fun createSimpleDateFormat(): SimpleDateFormat{
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)

    }
}