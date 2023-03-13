package net.pantasystem.milktea.data.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.*
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.delegate.FindRenoteMuteAndUpdateMemCacheDelegate
import net.pantasystem.milktea.data.infrastructure.user.renote.mute.delegate.FindRenoteMuteAndUpdateMemCacheDelegateImpl
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
    internal abstract fun bindRenoteMuteRepository(impl: RenoteMuteRepositoryImpl): RenoteMuteRepository

    @Binds
    @Singleton
    abstract fun bindIsSupportRenoteMuteInstance(impl: IsSupportRenoteMuteInstanceImpl): IsSupportRenoteMuteInstance

    @Binds
    @Singleton
    internal abstract fun bindFindRenoteMuteAndUpdateMemCacheDelegate(impl: FindRenoteMuteAndUpdateMemCacheDelegateImpl): FindRenoteMuteAndUpdateMemCacheDelegate
}