package jp.panta.misskeyandroidclient.model

import jp.panta.misskeyandroidclient.GsonFactory
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.api.MisskeyAuthAPI
import jp.panta.misskeyandroidclient.model.api.Version
import jp.panta.misskeyandroidclient.model.v12.MisskeyAPIV12
import jp.panta.misskeyandroidclient.model.v12.MisskeyAPIV12DiffImpl
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

    fun build(baseUrl: String, version: Version): MisskeyAPI{
        return when{
            version.isInRange(Version.Companion.Major.V_10) ->{
                Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create(GsonFactory.create()))
                    .build()
                    .create(jp.panta.misskeyandroidclient.model.v10.MisskeyAPIV10::class.java)
            }
            version.isInRange(Version.Companion.Major.V_11) ->{
                build(baseUrl)
            }
            version.isInRange(Version.Companion.Major.V_12) ->{
                val misskeyAPIV12 = build(baseUrl)
                val misskeyAPI12DiffImpl = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create(GsonFactory.create()))
                    .build()
                    .create(MisskeyAPIV12DiffImpl::class.java)
                return MisskeyAPIV12(misskeyAPIV12, misskeyAPI12DiffImpl)
            }
            else ->{
                build(baseUrl)
            }
        }
    }
}

