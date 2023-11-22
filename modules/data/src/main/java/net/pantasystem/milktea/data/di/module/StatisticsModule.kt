package net.pantasystem.milktea.data.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.infrastructure.statistics.InAppPostCounterRepositoryImpl
import net.pantasystem.milktea.model.statistics.InAppPostCounterRepository
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class StatisticsModule {

    @Binds
    @Singleton
    abstract fun bindInAppPostCounterRepository(impl: InAppPostCounterRepositoryImpl): InAppPostCounterRepository
}