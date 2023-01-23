package net.pantasystem.milktea.data.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.infrastructure.search.SearchHistoryRepositoryImpl
import net.pantasystem.milktea.model.search.SearchHistoryRepository
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class SearchBindsModule {
    @Binds
    @Singleton
    abstract fun bindSearchHistoryRepository(impl: SearchHistoryRepositoryImpl): SearchHistoryRepository
}