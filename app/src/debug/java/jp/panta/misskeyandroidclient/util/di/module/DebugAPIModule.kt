package jp.panta.misskeyandroidclient.util.di.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.util.FlipperSetupManagerImpl
import net.pantasystem.milktea.api.misskey.DefaultOkHttpClientProvider
import net.pantasystem.milktea.api.misskey.OkHttpClientProvider
import okhttp3.OkHttpClient
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DebugAPIModule {

    @Singleton
    @Provides
    fun provideOkHttpClientProvider(
        flipperSetupManager: FlipperSetupManagerImpl
    ): OkHttpClientProvider {
        val builder = DefaultOkHttpClientProvider().client.newBuilder()

        flipperSetupManager.applyNetworkFlipperPlugin(builder)
        val client = builder.build()
        return object : OkHttpClientProvider {
            override fun get(): OkHttpClient {
                return client
            }
        }
    }
}