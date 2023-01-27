package net.pantasystem.milktea.data.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.infrastructure.instance.MastodonInstanceInfoRepositoryImpl
import net.pantasystem.milktea.model.instance.MastodonInstanceInfoRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MastodonInstanceInfoModule {

    @Binds
    @Singleton
    abstract fun bindMastodonInfoRepository(impl: MastodonInstanceInfoRepositoryImpl): MastodonInstanceInfoRepository
}