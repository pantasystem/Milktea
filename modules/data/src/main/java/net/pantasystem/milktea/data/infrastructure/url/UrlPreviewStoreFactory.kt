package net.pantasystem.milktea.data.infrastructure.url


import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import net.pantasystem.milktea.api.misskey.MisskeyAPIServiceBuilder
import net.pantasystem.milktea.api.misskey.OkHttpClientProvider
import net.pantasystem.milktea.data.infrastructure.url.db.UrlPreviewDAO
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.url.UrlPreviewStore
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

class UrlPreviewStoreFactory(
    private val urlPreviewDAO: UrlPreviewDAO,
    private var summalyUrl: String? = null,
    var account: Account? = null,
    private val misskeyAPIServiceBuilder: MisskeyAPIServiceBuilder,
    private val okHttpClient: OkHttpClientProvider,
) {


    fun create(): UrlPreviewStore {
        val url = account?.normalizedInstanceUri
            ?: summalyUrl
        return UrlPreviewMediatorStore(urlPreviewDAO, createUrlPreviewStore(url))
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun createUrlPreviewStore(url: String?): UrlPreviewStore {
        return MisskeyUrlPreviewStore(
            Retrofit.Builder()
                .baseUrl(url!!)
                .addConverterFactory(misskeyAPIServiceBuilder.json.asConverterFactory("application/json".toMediaType()))
                .client(okHttpClient.get())
                .build()
                .create(RetrofitMisskeyUrlPreview::class.java)
        )
    }
}