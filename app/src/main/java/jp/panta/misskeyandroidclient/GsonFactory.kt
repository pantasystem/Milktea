package jp.panta.misskeyandroidclient

import com.google.gson.*
import kotlinx.datetime.Instant
import java.lang.reflect.Type
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