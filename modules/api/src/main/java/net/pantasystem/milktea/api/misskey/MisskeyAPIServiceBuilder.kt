package net.pantasystem.milktea.api.misskey

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import net.pantasystem.milktea.api.misskey.v10.MisskeyAPIV10
import net.pantasystem.milktea.api.misskey.v10.MisskeyAPIV10Diff
import net.pantasystem.milktea.api.misskey.v11.MisskeyAPIV11
import net.pantasystem.milktea.api.misskey.v11.MisskeyAPIV11Diff
import net.pantasystem.milktea.api.misskey.v12.MisskeyAPIV12
import net.pantasystem.milktea.api.misskey.v12.MisskeyAPIV12Diff
import net.pantasystem.milktea.api.misskey.v12_75_0.MisskeyAPIV1275
import net.pantasystem.milktea.api.misskey.v12_75_0.MisskeyAPIV1275Diff
import net.pantasystem.milktea.model.instance.Version
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

const val READ_TIMEOUT_S = 30L
const val CONNECTION_TIMEOUT_S = 30L
const val WRITE_TIMEOUT_S = 30L

interface OkHttpClientProvider {
    fun get(): OkHttpClient
    fun create(): OkHttpClient
}

class DefaultOkHttpClientProvider : OkHttpClientProvider {
    val client = OkHttpClient.Builder()
    .connectTimeout(CONNECTION_TIMEOUT_S, TimeUnit.SECONDS)
    .writeTimeout(WRITE_TIMEOUT_S, TimeUnit.SECONDS)
    .readTimeout(READ_TIMEOUT_S, TimeUnit.SECONDS)
    .build()
    override fun get(): OkHttpClient {
        return client
    }

    override fun create(): OkHttpClient {
        return OkHttpClient.Builder()
            .build()
    }
}
@OptIn(ExperimentalSerializationApi::class)
@Singleton
class MisskeyAPIServiceBuilder @Inject constructor(
    private val okHttpClientProvider: OkHttpClientProvider
){

    private val okHttpClient by lazy {
        okHttpClientProvider.get()
    }

    val json = Json {
        ignoreUnknownKeys = true
    }


    fun build(baseUrl: String): MisskeyAPI =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .client(okHttpClient)
            .build()
            .create(MisskeyAPI::class.java)

    fun buildAuthAPI(url: String): MisskeyAuthAPI =
        Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .client(okHttpClient)
            .build()
            .create(MisskeyAuthAPI::class.java)

    fun build(baseUrl: String, version: Version): MisskeyAPI {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        return when{
            version.isInRange(Version.Major.V_10) ->{
                val diff = retrofit.create(MisskeyAPIV10Diff::class.java)
                return MisskeyAPIV10(build(baseUrl), diff)
            }
            version.isInRange(Version.Major.V_11) || version.isInRange(Version.Major.V_12) || version.isInRange(Version.Major.V_13) ->{
                val baseAPI = build(baseUrl)
                val misskeyAPIV11Diff = retrofit.create(MisskeyAPIV11Diff::class.java)
                if(version.isInRange(Version.Major.V_12) || version.isInRange(Version.Major.V_13)){
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
                build(baseUrl)
            }
        }
    }
}

