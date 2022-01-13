package jp.panta.misskeyandroidclient.api

import jp.panta.misskeyandroidclient.GsonFactory
import jp.panta.misskeyandroidclient.model.api.Version
import jp.panta.misskeyandroidclient.api.v10.MisskeyAPIV10
import jp.panta.misskeyandroidclient.api.v10.MisskeyAPIV10Diff
import jp.panta.misskeyandroidclient.api.v11.MisskeyAPIV11
import jp.panta.misskeyandroidclient.api.v11.MisskeyAPIV11Diff
import jp.panta.misskeyandroidclient.api.v12.MisskeyAPIV12
import jp.panta.misskeyandroidclient.api.v12.MisskeyAPIV12Diff
import jp.panta.misskeyandroidclient.api.v12_75_0.MisskeyAPIV1275
import jp.panta.misskeyandroidclient.api.v12_75_0.MisskeyAPIV1275Diff
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject

object MisskeyAPIServiceBuilder : MisskeyAPIServiceFactory {
    private const val READ_TIMEOUT_S = 30L
    private const val WRITE_TIMEOUT_S = 30L
    private const val CONNECTION_TIMEOUT_S = 30L
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(CONNECTION_TIMEOUT_S, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT_S, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_S, TimeUnit.SECONDS)
        .build()

    override fun create(baseUrl: String): MisskeyAPI =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(GsonFactory.create()))
            .client(okHttpClient)
            .build()
            .create(MisskeyAPI::class.java)

    fun buildAuthAPI(url: String): MisskeyAuthAPI =
        Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create(GsonFactory.create()))
            .client(okHttpClient)
            .build()
            .create(MisskeyAuthAPI::class.java)

    override fun create(baseUrl: String, version: Version): MisskeyAPI {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(GsonFactory.create()))
            .build()
        return when{
            version.isInRange(Version.Major.V_10) ->{
                val diff = retrofit.create(MisskeyAPIV10Diff::class.java)
                return MisskeyAPIV10(create(baseUrl), diff)
            }
            version.isInRange(Version.Major.V_11)
                    || version.isInRange(Version.Major.V_12) ->{
                val baseAPI = create(baseUrl)
                val misskeyAPIV11Diff = retrofit.create(MisskeyAPIV11Diff::class.java)
                if(version.isInRange(Version.Major.V_12)){
                    val misskeyAPI12DiffImpl = retrofit.create(MisskeyAPIV12Diff::class.java)
                    if(version >= Version("12.75.0")) {
                        val misskeyAPIV1275Diff = retrofit.create(MisskeyAPIV1275Diff::class.java)
                        MisskeyAPIV1275(baseAPI, misskeyAPIV1275Diff, misskeyAPI12DiffImpl, misskeyAPIV11Diff)
                    }else{
                        MisskeyAPIV12(baseAPI, misskeyAPI12DiffImpl, misskeyAPIV11Diff)
                    }

                }else{
                    MisskeyAPIV11(baseAPI, misskeyAPIV11Diff)
                }
            }
            else ->{
                create(baseUrl)
            }
        }
    }
}

