package net.pantasystem.milktea.data.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.app_store.drive.DriveDirectoryPagingStore
import net.pantasystem.milktea.data.infrastructure.drive.DriveDirectoryPagingStoreImpl
import net.pantasystem.milktea.data.infrastructure.drive.DriveDirectoryRepositoryImpl
import net.pantasystem.milktea.model.drive.DriveDirectoryRepository
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class DriveDirectoryModule {

    @Singleton
    @Binds
    abstract fun provideDriveDirectoryRepository(impl: DriveDirectoryRepositoryImpl): DriveDirectoryRepository

    @Singleton
    @Binds
    abstract fun provideDriveDirectoryPagingStore(impl: DriveDirectoryPagingStoreImpl): DriveDirectoryPagingStore
}