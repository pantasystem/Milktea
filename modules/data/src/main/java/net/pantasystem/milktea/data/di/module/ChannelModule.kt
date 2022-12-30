package net.pantasystem.milktea.data.di.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.channel.ChannelAPIAdapter
import net.pantasystem.milktea.data.infrastructure.channel.ChannelAPIAdapterWebImpl
import net.pantasystem.milktea.data.infrastructure.channel.ChannelRepositoryImpl
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.channel.ChannelRepository
import net.pantasystem.milktea.model.channel.ChannelStateModel
import net.pantasystem.milktea.model.channel.ChannelStateModelOnMemory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChannelModule {

    @Provides
    @Singleton
    fun provideChannelStateModel(): ChannelStateModel {
        return ChannelStateModelOnMemory()
    }

    @Provides
    @Singleton
    fun provideChannelAPIAdapter(
        accountRepository: AccountRepository,
        misskeyAPIProvider: MisskeyAPIProvider
    ): ChannelAPIAdapter {
        return ChannelAPIAdapterWebImpl(accountRepository, misskeyAPIProvider)
    }

    @Provides
    @Singleton
    fun provideChannelRepository(
        @IODispatcher ioDispatcher: CoroutineDispatcher,
        channelStateModel: ChannelStateModel,
        accountRepository: AccountRepository,
        channelAPIAdapter: ChannelAPIAdapter,
        ): ChannelRepository {
        return ChannelRepositoryImpl(
            channelStateModel = channelStateModel,
            accountRepository = accountRepository,
            channelAPIAdapter = channelAPIAdapter,
            ioDispatcher = ioDispatcher
        )
    }
}