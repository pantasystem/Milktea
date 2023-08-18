package net.pantasystem.milktea.data.di.module

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.data.infrastructure.emoji.EmojiEventHandlerImpl
import net.pantasystem.milktea.data.infrastructure.note.NoteCaptureAPIAdapterImpl
import net.pantasystem.milktea.data.infrastructure.note.NoteCaptureAPIWithAccountProvider
import net.pantasystem.milktea.data.infrastructure.note.NoteCaptureAPIWithAccountProviderImpl
import net.pantasystem.milktea.data.infrastructure.note.NoteDataSourceAdder
import net.pantasystem.milktea.data.infrastructure.notification.impl.NotificationStreamingImpl
import net.pantasystem.milktea.data.streaming.ChannelAPIWithAccountProvider
import net.pantasystem.milktea.data.streaming.SocketWithAccountProvider
import net.pantasystem.milktea.data.streaming.StreamingAPIProvider
import net.pantasystem.milktea.data.streaming.impl.SocketWithAccountProviderImpl
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.emoji.CustomEmojiAspectRatioDataSource
import net.pantasystem.milktea.model.emoji.EmojiEventHandler
import net.pantasystem.milktea.model.image.ImageCacheRepository
import net.pantasystem.milktea.model.note.NoteCaptureAPIAdapter
import net.pantasystem.milktea.model.note.NoteDataSource
import net.pantasystem.milktea.model.notification.NotificationStreaming
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

    @Binds
    @Singleton
    abstract fun bindEmojiEventHandler(impl: EmojiEventHandlerImpl): EmojiEventHandler

    @Binds
    @Singleton
    abstract fun bindNotificationStreaming(impl: NotificationStreamingImpl): NotificationStreaming
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
        return ChannelAPIWithAccountProvider(
            socketWithAccountProvider,
            loggerFactory
        )
    }

    @Singleton
    @Provides
    fun provideNoteCaptureAPIAdapter(
        accountRepository: AccountRepository,
        coroutineScope: CoroutineScope,
        loggerFactory: Logger.Factory,
        noteCaptureAPIWithAccountProvider: NoteCaptureAPIWithAccountProvider,
        noteDataSource: NoteDataSource,
        noteDataSourceAdder: NoteDataSourceAdder,
        streamingAPIProvider: StreamingAPIProvider,
        customEmojiAspectRatioDataSource: CustomEmojiAspectRatioDataSource,
        imageCacheRepository: ImageCacheRepository,
    ): NoteCaptureAPIAdapter {
        return NoteCaptureAPIAdapterImpl(
            accountRepository = accountRepository,
            cs = coroutineScope,
            loggerFactory = loggerFactory,
            noteCaptureAPIWithAccountProvider = noteCaptureAPIWithAccountProvider,
            noteDataSource = noteDataSource,
            dispatcher = Dispatchers.IO,
            noteDataSourceAdder = noteDataSourceAdder,
            streamingAPIProvider = streamingAPIProvider,
            customEmojiAspectRatioDataSource = customEmojiAspectRatioDataSource,
            imageCacheRepository = imageCacheRepository,
        )
    }
}