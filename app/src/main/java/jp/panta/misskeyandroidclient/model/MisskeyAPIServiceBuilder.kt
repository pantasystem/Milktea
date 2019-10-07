package jp.panta.misskeyandroidclient.model

import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object MisskeyAPIServiceBuilder {
    fun build(baseUrl: String): MisskeyAPI =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().build())
            .build()
            .create(MisskeyAPI::class.java)
}

