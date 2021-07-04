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
        .registerTypeAdapter(Instant::class.java, object : JsonDeserializer<Instant?>{
            override fun deserialize(
                json: JsonElement?,
                typeOfT: Type?,
                context: JsonDeserializationContext?
            ): Instant? {
                if(json == null || json.isJsonNull) {
                    return null
                }
                return Instant.parse(json.asString)
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