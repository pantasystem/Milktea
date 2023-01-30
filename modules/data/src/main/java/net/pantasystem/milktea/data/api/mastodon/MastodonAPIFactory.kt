package net.pantasystem.milktea.data.api.mastodon

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import net.pantasystem.milktea.api.mastodon.MastodonAPI
import net.pantasystem.milktea.api.misskey.OkHttpClientProvider
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MastodonAPIFactory @Inject constructor(
    val okHttpProvider: OkHttpClientProvider,
){

    val json = Json { ignoreUnknownKeys = true }

    private val sharedOkHttp = okHttpProvider.get()

    private var okHttpClientMap = mapOf<MastodonAPIProvider.Key, OkHttpClient>()

    @OptIn(ExperimentalSerializationApi::class)
    fun build(baseURL: String, token: String?): MastodonAPI {
        val okHttp = getOkHttp(baseURL, token)
        return Retrofit.Builder()
            .baseUrl(baseURL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .client(okHttp)
            .build()
            .create(MastodonAPI::class.java)

    }

    fun getOkHttp(baseURL: String, token: String?): OkHttpClient {
        return if (token == null) {
            sharedOkHttp
        } else {
            synchronized(this) {
                val key = MastodonAPIProvider.Key(instanceBaseURL = baseURL, token = token)
                var client = okHttpClientMap[key]
                if (client == null) {
                    client = okHttpProvider.get().newBuilder()
                        .addInterceptor {
                            val request = it.request()
                            val newReq = request.newBuilder()
                                .header("Authorization", "Bearer $token")
                                .build()
                            it.proceed(newReq)
                        }.build()
                    okHttpClientMap = okHttpClientMap + (key to client)
                }
                client

            }

        }
    }


}