package net.pantasystem.milktea.data.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.infrastructure.image.ImageCacheRepositoryImpl
import net.pantasystem.milktea.model.image.ImageCacheRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ImageCacheBindModule {

    @Binds
    @Singleton
    abstract fun bindImageCacheRepository(impl: ImageCacheRepositoryImpl): ImageCacheRepository
}