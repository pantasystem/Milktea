package jp.panta.misskeyandroidclient.di.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.model.channel.ChannelAPIAdapter
import net.pantasystem.milktea.data.model.channel.impl.ChannelAPIAdapterWebImpl
import net.pantasystem.milktea.data.model.channel.impl.ChannelRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChannelModule {

    @Provides
    @Singleton
    fun provideChannelStateModel(): net.pantasystem.milktea.model.channel.ChannelStateModel {
        return net.pantasystem.milktea.model.channel.ChannelStateModelOnMemory()
    }

    @Provides
    @Singleton
    fun provideChannelAPIAdapter(
        accountRepository: net.pantasystem.milktea.model.account.AccountRepository,
        encryption: Encryption,
        misskeyAPIProvider: net.pantasystem.milktea.api.misskey.MisskeyAPIProvider
    ): net.pantasystem.milktea.model.channel.ChannelAPIAdapter {
        return ChannelAPIAdapterWebImpl(accountRepository, misskeyAPIProvider, encryption)
    }

    @Provides
    @Singleton
    fun provideChannelRepository(
        channelStateModel: net.pantasystem.milktea.model.channel.ChannelStateModel,
        accountRepository: net.pantasystem.milktea.model.account.AccountRepository,
        channelAPIAdapter: net.pantasystem.milktea.model.channel.ChannelAPIAdapter,
        ): net.pantasystem.milktea.model.channel.ChannelRepository {
        return ChannelRepositoryImpl(
            channelStateModel = channelStateModel,
            accountRepository = accountRepository,
            channelAPIAdapter = channelAPIAdapter
        )
    }
}