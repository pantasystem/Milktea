package net.pantasystem.milktea.common

import com.google.gson.*
import kotlinx.datetime.Instant
import java.lang.reflect.Type

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

}