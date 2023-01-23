package net.pantasystem.milktea.data.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.infrastructure.ap.ApResolverRepositoryImpl
import net.pantasystem.milktea.model.ap.ApResolverRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ApResolverModule {

    @Binds
    @Singleton
    abstract fun bindApResolverRepository(impl: ApResolverRepositoryImpl): ApResolverRepository

}