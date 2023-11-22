package net.pantasystem.milktea.data.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.infrastructure.statistics.InAppPostCounterRepositoryImpl
import net.pantasystem.milktea.data.infrastructure.statistics.LastlyInAppReviewShownRepositoryImpl
import net.pantasystem.milktea.model.statistics.InAppPostCounterRepository
import net.pantasystem.milktea.model.statistics.LastlyInAppReviewShownRepository
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class StatisticsModule {

    @Binds
    @Singleton
    abstract fun bindInAppPostCounterRepository(impl: InAppPostCounterRepositoryImpl): InAppPostCounterRepository

    @Binds
    @Singleton
    abstract fun bindLastlyInAppReviewShownRepository(impl: LastlyInAppReviewShownRepositoryImpl): LastlyInAppReviewShownRepository
}