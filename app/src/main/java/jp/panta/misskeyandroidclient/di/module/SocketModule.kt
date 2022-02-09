package jp.panta.misskeyandroidclient.di.module.streaming

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.model.notes.NoteCaptureAPIAdapter
import jp.panta.misskeyandroidclient.model.notes.NoteCaptureAPIWithAccountProvider
import jp.panta.misskeyandroidclient.model.notes.NoteCaptureAPIWithAccountProviderImpl
import jp.panta.misskeyandroidclient.model.notes.NoteDataSource
import jp.panta.misskeyandroidclient.streaming.SocketWithAccountProvider
import jp.panta.misskeyandroidclient.streaming.channel.ChannelAPIWithAccountProvider
import jp.panta.misskeyandroidclient.streaming.impl.SocketWithAccountProviderImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class SocketBindsModule {

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


@InstallIn(SingletonComponent::class)
@Module
object SocketModule {
    @Singleton
    @Provides
    fun provideChannelAPIProvider(
        loggerFactory: Logger.Factory,
        socketWithAccountProvider: SocketWithAccountProvider
    ): ChannelAPIWithAccountProvider {
        return ChannelAPIWithAccountProvider(socketWithAccountProvider, loggerFactory)
    }

    @Singleton
    @Provides
    fun provideNoteCaptureAPIAdapter(
        accountRepository: AccountRepository,
        coroutineScope: CoroutineScope,
        loggerFactory: Logger.Factory,
        noteCaptureAPIWithAccountProvider: NoteCaptureAPIWithAccountProvider,
        noteDataSource: NoteDataSource,
    ): NoteCaptureAPIAdapter {
        return NoteCaptureAPIAdapter(
            accountRepository = accountRepository,
            cs = coroutineScope,
            loggerFactory = loggerFactory,
            noteCaptureAPIWithAccountProvider = noteCaptureAPIWithAccountProvider,
            noteDataSource = noteDataSource,
            dispatcher = Dispatchers.IO
        )
    }
}