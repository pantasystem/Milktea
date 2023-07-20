package net.pantasystem.milktea.api.misskey

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
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
        return build(baseUrl)

    }
}

