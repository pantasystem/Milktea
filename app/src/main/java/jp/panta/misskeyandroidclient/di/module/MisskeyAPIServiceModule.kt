package jp.panta.misskeyandroidclient.di.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.api.MisskeyAPIServiceBuilder
import jp.panta.misskeyandroidclient.api.MisskeyAPIServiceFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class MisskeyAPIServiceModule {

    @Provides
    @Singleton
    fun providesMisskeyAPIFactory() : MisskeyAPIServiceFactory {
        return MisskeyAPIServiceBuilder
    }
}