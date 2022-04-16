package net.pantasystem.milktea.data.api.mastodon

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MastodonAPIFactory @Inject constructor(){

    val json = Json { ignoreUnknownKeys = true }

    private val sharedOkHttp = OkHttpClient()

    @OptIn(ExperimentalSerializationApi::class)
    fun build(baseURL: String, token: String?): MastodonAPI {
        val okHttp = if (token == null) {
            sharedOkHttp
        } else {
            OkHttpClient.Builder()
                .addInterceptor {
                    val request = it.request()
                    val newReq = request.newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                    it.proceed(newReq)
                }.build()
        }
        return Retrofit.Builder()
            .baseUrl(baseURL)
            .addConverterFactory(json.asConverterFactory(MediaType.get("application/json")))
            .client(okHttp)
            .build()
            .create(MastodonAPI::class.java)

    }


}