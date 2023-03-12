package net.pantasystem.milktea.data.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.RenoteMuteApiAdapter
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.RenoteMuteApiAdapterImpl
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.RenoteMuteRepositoryImpl
import net.pantasystem.milktea.model.user.renote.mute.RenoteMuteRepository
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class RenoteMuteModule {

    @Binds
    @Singleton
    abstract fun bindRenoteMuteApiAdapter(impl: RenoteMuteApiAdapterImpl): RenoteMuteApiAdapter

    @Binds
    @Singleton
    abstract fun bindRenoteMuteRepository(impl: RenoteMuteRepositoryImpl): RenoteMuteRepository

}