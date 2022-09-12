package jp.panta.misskeyandroidclient.di.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.api.misskey.DefaultOkHttpClientProvider
import net.pantasystem.milktea.api.misskey.OkHttpClientProvider
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object ReleaseAPIModule {

    @Singleton
    @Provides
    fun provideOkHttpClientProvider(): OkHttpClientProvider {
        return DefaultOkHttpClientProvider()
    }
}