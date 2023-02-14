package net.pantasystem.milktea.api.misskey

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import net.pantasystem.milktea.api.misskey.infos.InstanceInfosResponse
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import javax.inject.Inject
import javax.inject.Singleton


interface InstanceInfosAPI {

    @GET("instances.json")
    suspend fun getInstances(): Response<InstanceInfosResponse>
}

@Singleton
class InstanceInfoAPIBuilder @Inject constructor(val okHttpClientProvider: OkHttpClientProvider) {
    private val json = Json {
        ignoreUnknownKeys = true
    }
    @OptIn(ExperimentalSerializationApi::class)
    private val retrofitBuilder by lazy {
        Retrofit.Builder()
            .baseUrl("https://instanceapp.misskey.page")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .client(okHttpClientProvider.get())
            .build()
    }
    fun build(): InstanceInfosAPI {
        return retrofitBuilder.create(InstanceInfosAPI::class.java)
    }
}