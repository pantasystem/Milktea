package net.pantasystem.milktea.data.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.infrastructure.filter.MastodonWordFilterRepositoryImpl
import net.pantasystem.milktea.model.filter.MastodonWordFilterRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WordFilterModule {

    @Binds
    @Singleton
    abstract fun bindMastodonWordFilterRepository(impl: MastodonWordFilterRepositoryImpl): MastodonWordFilterRepository

}