package net.pantasystem.milktea.api.milktea.instance.ticker

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import net.pantasystem.milktea.api.misskey.OkHttpClientProvider
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstanceTickerAPIServiceBuilder @Inject constructor(
    val okHttpClientProvider: OkHttpClientProvider
) {

    val json = Json {
        ignoreUnknownKeys = true
    }

    fun build(baseUrl: String): InstanceTickerAPIService {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .client(okHttpClientProvider.get())
            .build()
            .create(InstanceTickerAPIService::class.java)
    }
}