package net.pantasystem.milktea.data.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.infrastructure.url.UrlPreviewStoreProviderImpl
import net.pantasystem.milktea.model.url.UrlPreviewStoreProvider
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class UrlPreviewModule {

    @Binds
    @Singleton
    abstract fun bindUrlPreviewStoreProvider(impl: UrlPreviewStoreProviderImpl): UrlPreviewStoreProvider
}