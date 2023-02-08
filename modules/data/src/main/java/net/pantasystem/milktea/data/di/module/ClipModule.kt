package net.pantasystem.milktea.data.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.infrastructure.clip.ClipRepositoryImpl
import net.pantasystem.milktea.model.clip.ClipRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ClipBindModule {

    @Binds
    @Singleton
    abstract fun bindClipRepository(impl: ClipRepositoryImpl): ClipRepository
}