package jp.panta.misskeyandroidclient.api.mastodon

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.Account
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MastodonAPIFactory @Inject constructor(
    val encryption: Encryption
){

    val json = Json { ignoreUnknownKeys = true }

    private val sharedOkHttp = OkHttpClient()

    @OptIn(ExperimentalSerializationApi::class)
    fun build(baseURL: String, account: Account? = null): MastodonAPI {
        val okHttp = if (account == null) {
            sharedOkHttp
        } else {
            OkHttpClient.Builder()
                .addInterceptor {
                    val request = it.request()
                    val newReq = request.headers["Authorization"]?.let {
                        request.newBuilder()
                            .header("Authorization", "Bearer ${account.getI(encryption)}")
                            .build()
                    }?: request
                    it.proceed(newReq)
                }.build()
        }
        return Retrofit.Builder()
            .baseUrl(account?.instanceDomain?: baseURL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .client(okHttp)
            .build()
            .create(MastodonAPI::class.java)

    }
}