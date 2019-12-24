package jp.panta.misskeyandroidclient.model

import com.google.gson.GsonBuilder
import jp.panta.misskeyandroidclient.GsonFactory
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.api.MisskeyAuthAPI
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object MisskeyAPIServiceBuilder {
    fun build(baseUrl: String): MisskeyAPI =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(GsonFactory.create()))
            .client(OkHttpClient.Builder().build())
            .build()
            .create(MisskeyAPI::class.java)

    fun buildAuthAPI(url: String): MisskeyAuthAPI =
        Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create(GsonFactory.create()))
            .client(OkHttpClient.Builder().build())
            .build()
            .create(MisskeyAuthAPI::class.java)
}

