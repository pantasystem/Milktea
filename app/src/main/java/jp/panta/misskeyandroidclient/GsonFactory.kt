package jp.panta.misskeyandroidclient

import android.content.Intent
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
        .registerTypeAdapter(Instant::class.java, object : TypeAdapter<Instant?>() {
            override fun read(jsonReader: JsonReader?): Instant? {
                if(jsonReader?.peek() == JsonToken.NULL) {
                    return null
                }
                return jsonReader?.nextString()?.let { dateTime ->
                    return Instant.parse(dateTime)
                } ?: Clock.System.now()
            }

            override fun write(out: JsonWriter?, value: Instant?) {
                out?.value(value.toString())
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