package jp.panta.misskeyandroidclient

import com.google.gson.*
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import java.lang.reflect.Type
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object GsonFactory {
    private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .registerTypeAdapter(LocalDateTime::class.java, object : TypeAdapter<LocalDateTime?>() {
            override fun read(jsonReader: JsonReader?): LocalDateTime? {
                if(jsonReader?.peek() == JsonToken.NULL) {
                    return null
                }
                return jsonReader?.nextString()?.let { dateTime ->
                    LocalDateTime.parse(dateTime)
                } ?: Clock.System.now().toLocalDateTime(TimeZone.UTC)
            }

            override fun write(out: JsonWriter?, value: LocalDateTime?) {
                out?.value(value?.toInstant(TimeZone.UTC).toString())
            }
        })
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