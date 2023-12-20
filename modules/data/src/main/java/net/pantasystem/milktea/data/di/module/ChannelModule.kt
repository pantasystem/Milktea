package net.pantasystem.milktea.data.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.infrastructure.channel.ChannelAPIAdapter
import net.pantasystem.milktea.data.infrastructure.channel.ChannelAPIAdapterWebImpl
import net.pantasystem.milktea.data.infrastructure.channel.ChannelRepositoryImpl
import net.pantasystem.milktea.model.channel.ChannelRepository
import net.pantasystem.milktea.model.channel.ChannelStateModel
import net.pantasystem.milktea.model.channel.ChannelStateModelOnMemory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ChannelModule {

    @Binds
    @Singleton
    abstract fun bindChannelStateModel(channelStateModel: ChannelStateModelOnMemory): ChannelStateModel

    @Binds
    @Singleton
    abstract fun bindChannelRepository(channelRepositoryImpl: ChannelRepositoryImpl): ChannelRepository

    @Binds
    @Singleton
    abstract fun bindChannelAPIAdapter(channelAPIAdapter: ChannelAPIAdapterWebImpl): ChannelAPIAdapter

}