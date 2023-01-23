package jp.panta.misskeyandroidclient.impl

import android.os.Build
import jp.panta.misskeyandroidclient.BuildConfig
import net.pantasystem.milktea.api.misskey.DefaultOkHttpClientProvider
import net.pantasystem.milktea.api.misskey.OkHttpClientProvider
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OkHttpClientProviderImpl @Inject constructor(): OkHttpClientProvider {
    private val client by lazy {
        DefaultOkHttpClientProvider().client.newBuilder()
            .addInterceptor { interceptor ->
                val request = interceptor.request().newBuilder()
                    .addHeader("User-Agent", "Milktae/:${BuildConfig.VERSION_NAME} Android/${Build.VERSION.RELEASE}")
                    .build()
                interceptor.proceed(request)
            }.build()
    }
    override fun get(): OkHttpClient {
        return client
    }

    override fun create(): OkHttpClient {
        return OkHttpClient()
    }
}