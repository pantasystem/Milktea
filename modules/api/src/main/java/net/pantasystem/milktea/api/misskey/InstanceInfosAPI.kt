package net.pantasystem.milktea.api.misskey

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import net.pantasystem.milktea.api.misskey.infos.SimpleInstanceInfo
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Inject
import javax.inject.Singleton


interface InstanceInfosAPI {

    @GET("instances")
    suspend fun getInstances(
        @Query("name") name: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null,
    ): Response<List<SimpleInstanceInfo>>
}

@Singleton
class InstanceInfoAPIBuilder @Inject constructor(val okHttpClientProvider: OkHttpClientProvider) {
    private val json = Json {
        ignoreUnknownKeys = true
    }
    @OptIn(ExperimentalSerializationApi::class)
    private val retrofitBuilder by lazy {
        Retrofit.Builder()
            .baseUrl("https://milktea-instance-suggestions.milktea.workers.dev")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .client(okHttpClientProvider.get())
            .build()
    }


    fun build(): InstanceInfosAPI {
        return retrofitBuilder.create(InstanceInfosAPI::class.java)
    }

}