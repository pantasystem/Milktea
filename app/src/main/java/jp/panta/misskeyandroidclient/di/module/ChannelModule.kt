package jp.panta.misskeyandroidclient.di.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.api.misskey.MisskeyAPIProvider
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.model.channel.ChannelAPIAdapter
import jp.panta.misskeyandroidclient.model.channel.ChannelRepository
import jp.panta.misskeyandroidclient.model.channel.ChannelStateModel
import jp.panta.misskeyandroidclient.model.channel.ChannelStateModelOnMemory
import jp.panta.misskeyandroidclient.model.channel.impl.ChannelAPIAdapterWebImpl
import jp.panta.misskeyandroidclient.model.channel.impl.ChannelRepositoryImpl
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
        encryption: Encryption,
        misskeyAPIProvider: MisskeyAPIProvider
    ): ChannelAPIAdapter {
        return ChannelAPIAdapterWebImpl(accountRepository, misskeyAPIProvider, encryption)
    }

    @Provides
    @Singleton
    fun provideChannelRepository(
        channelStateModel: ChannelStateModel,
        accountRepository: AccountRepository,
        channelAPIAdapter: ChannelAPIAdapter,
        ): ChannelRepository {
        return ChannelRepositoryImpl(
            channelStateModel = channelStateModel,
            accountRepository = accountRepository,
            channelAPIAdapter = channelAPIAdapter
        )
    }
}