package jp.panta.misskeyandroidclient.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.impl.OkHttpClientProviderImpl
import net.pantasystem.milktea.api.misskey.OkHttpClientProvider

@InstallIn(SingletonComponent::class)
@Module
abstract class ReleaseAPIModule {

    @Binds
    abstract fun bindOkHttpClientProvider(
        impl: OkHttpClientProviderImpl
    ): OkHttpClientProvider
}