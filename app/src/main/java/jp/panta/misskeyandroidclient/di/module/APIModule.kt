package jp.panta.misskeyandroidclient.di.module

import android.content.Context
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.BuildConfig
import jp.panta.misskeyandroidclient.util.FlipperSetupManager
import net.pantasystem.milktea.api.misskey.CONNECTION_TIMEOUT_S
import net.pantasystem.milktea.api.misskey.OkHttpClientProvider
import net.pantasystem.milktea.api.misskey.READ_TIMEOUT_S
import net.pantasystem.milktea.api.misskey.WRITE_TIMEOUT_S
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object APIModule {

    @Singleton
    @Provides
    fun provideOkHttpClientProvider(
        @ApplicationContext context: Context
    ): OkHttpClientProvider {
        val builder = OkHttpClient.Builder()
            .connectTimeout(CONNECTION_TIMEOUT_S, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_S, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_S, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG && FlipperUtils.shouldEnableFlipper(context)) {
            builder.addInterceptor(FlipperOkhttpInterceptor(FlipperSetupManager.networkFlipperPlugin))
        }
        val client = builder.build()
        return object : OkHttpClientProvider {
            override fun get(): OkHttpClient {
                return client
            }
        }
    }
}