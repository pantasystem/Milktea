package jp.panta.misskeyandroidclient.api.mastodon.apps

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import jp.panta.misskeyandroidclient.api.mastodon.MastodonAPI
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.junit.Test
import retrofit2.Retrofit

class AppTest {

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun generateAuthUrl() {
        val contentType = "application/json".toMediaType()

        val json = Json { ignoreUnknownKeys = true }
        val api = Retrofit.Builder()
            .baseUrl("https://mastodon.social")
            .addConverterFactory(json.asConverterFactory(contentType))
            .client(OkHttpClient())
            .build()
            .create(MastodonAPI::class.java)
        runBlocking {
            val res = api.createApp(CreateApp(
                clientName = "Milktea",
                redirectUris = "http://hoge",
                scopes = "read write follow push"
            ))
            println(res)
            println(res.generateAuthUrl("https://mastodon.social", "read write follow push"))
        }
    }
}