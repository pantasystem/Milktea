package net.pantasystem.milktea.data.infrastructure.url


import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import net.pantasystem.milktea.api.misskey.MisskeyAPIServiceBuilder
import net.pantasystem.milktea.app_store.setting.UrlPreviewSourceSetting
import net.pantasystem.milktea.data.infrastructure.url.db.UrlPreviewDAO
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.url.UrlPreviewStore
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

class UrlPreviewStoreFactory(
    private val urlPreviewDAO: UrlPreviewDAO,
    sourceType: Int? = null,
    private var summalyUrl: String? = null,
    var account: Account? = null,
    private val misskeyAPIServiceBuilder: MisskeyAPIServiceBuilder
) {

    private var sourceType = sourceType ?: UrlPreviewSourceSetting.MISSKEY

    fun create(): UrlPreviewStore {
        val url = when (sourceType) {
            UrlPreviewSourceSetting.MISSKEY -> {
                account?.instanceDomain
                    ?: summalyUrl
            }
            UrlPreviewSourceSetting.SUMMALY -> {
                summalyUrl
                    ?: account?.instanceDomain
            }
            else -> null
        }
        return UrlPreviewMediatorStore(urlPreviewDAO, createUrlPreviewStore(url))
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun createUrlPreviewStore(url: String?): UrlPreviewStore {
        return when (sourceType) {
            UrlPreviewSourceSetting.MISSKEY, UrlPreviewSourceSetting.SUMMALY -> {
                try {
                    MisskeyUrlPreviewStore(
                        Retrofit.Builder()
                            .baseUrl(url!!)
                            .addConverterFactory(misskeyAPIServiceBuilder.json.asConverterFactory("application/json".toMediaType()))
                            .client(OkHttpClient.Builder().build())
                            .build()
                            .create(RetrofitMisskeyUrlPreview::class.java)
                    )
                } catch (e: Exception) {
                    JSoupUrlPreviewStore()
                }

            }
            else -> {
                JSoupUrlPreviewStore()
            }
        }
    }
}