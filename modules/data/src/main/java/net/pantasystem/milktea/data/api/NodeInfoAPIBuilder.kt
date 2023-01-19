package net.pantasystem.milktea.data.api

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import net.pantasystem.milktea.api.activitypub.NodeInfoAPI
import net.pantasystem.milktea.api.activitypub.WellKnownNodeInfoAPI
import net.pantasystem.milktea.api.misskey.OkHttpClientProvider
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import javax.inject.Inject

class NodeInfoAPIBuilder @Inject constructor(
    val okHttpClientProvider: OkHttpClientProvider,
) {

    val json = Json {
        ignoreUnknownKeys = true
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun build(): NodeInfoAPI {
        val okHttp = okHttpClientProvider.get()
        return Retrofit.Builder()
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .client(okHttp)
            .baseUrl("https://example.com/") // NOTE: 任意のURLを指定しないと怒られる
            .build()
            .create(NodeInfoAPI::class.java)
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun buildWellKnown(baseUrl: String): WellKnownNodeInfoAPI {
        val okHttp = okHttpClientProvider.get()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .client(okHttp)
            .build()
            .create(WellKnownNodeInfoAPI::class.java)
    }
}