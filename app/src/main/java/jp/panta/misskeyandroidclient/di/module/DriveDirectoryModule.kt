package jp.panta.misskeyandroidclient.di.module

import dagger.Binds
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.infrastructure.drive.DriveDirectoryRepositoryImpl
import net.pantasystem.milktea.model.drive.DriveDirectoryRepository
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
abstract class DriveDirectoryModule {

    @Singleton
    @Binds
    abstract fun provideDriveDirectoryRepository(impl: DriveDirectoryRepositoryImpl): DriveDirectoryRepository
}