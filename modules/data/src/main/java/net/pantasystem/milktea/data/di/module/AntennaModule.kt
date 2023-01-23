package net.pantasystem.milktea.data.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.infrastructure.antenna.AntennaRepositoryImpl
import net.pantasystem.milktea.model.antenna.AntennaRepository
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class AntennaBindModule {

    @Binds
    @Singleton
    abstract fun bindAntennaRepository(impl: AntennaRepositoryImpl): AntennaRepository

}