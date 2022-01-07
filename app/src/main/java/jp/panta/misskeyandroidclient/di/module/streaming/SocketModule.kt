package jp.panta.misskeyandroidclient.di.module.streaming

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.model.notes.NoteCaptureAPIWithAccountProvider
import jp.panta.misskeyandroidclient.model.notes.NoteCaptureAPIWithAccountProviderImpl
import jp.panta.misskeyandroidclient.streaming.SocketWithAccountProvider
import jp.panta.misskeyandroidclient.streaming.impl.SocketWithAccountProviderImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class SocketModule {

    @Binds
    @Singleton
    abstract fun provideSocketWithAccountProvider(
        socketWithAccountProviderImpl: SocketWithAccountProviderImpl
    ): SocketWithAccountProvider

    @Binds
    @Singleton
    abstract fun provideNoteCaptureAPIWithAccountProvider(
        provider: NoteCaptureAPIWithAccountProviderImpl
    ) : NoteCaptureAPIWithAccountProvider
}