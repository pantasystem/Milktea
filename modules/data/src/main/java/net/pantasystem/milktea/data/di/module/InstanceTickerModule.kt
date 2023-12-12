package net.pantasystem.milktea.data.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.infrastructure.instance.ticker.InstanceTickerRepositoryImpl
import net.pantasystem.milktea.model.instance.ticker.InstanceTickerRepository
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class InstanceTickerModule {

    @Binds
    @Singleton
    abstract fun bindInstanceTickerRepository(
        repository: InstanceTickerRepositoryImpl
    ): InstanceTickerRepository
}