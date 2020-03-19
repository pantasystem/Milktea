package jp.panta.misskeyandroidclient

import com.google.gson.Gson
import com.google.gson.GsonBuilder

object GsonFactory {
    private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create()

    fun create(): Gson{
        /*val gsonBuilder = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")*/
        return gson//gsonBuilder.create()
    }
}