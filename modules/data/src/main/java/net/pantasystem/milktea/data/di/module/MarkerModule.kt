package net.pantasystem.milktea.data.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.infrastructure.markers.MarkerRepositoryImpl
import net.pantasystem.milktea.model.markers.MarkerRepository

@InstallIn(SingletonComponent::class)
@Module
abstract class MarkerModule {

    @Binds
    abstract fun bindMarkerRepository(impl: MarkerRepositoryImpl): MarkerRepository

}