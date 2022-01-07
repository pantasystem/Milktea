package jp.panta.misskeyandroidclient.di.module.files

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.model.drive.DriveFileRepository
import jp.panta.misskeyandroidclient.model.drive.DriveFileRepositoryImpl
import jp.panta.misskeyandroidclient.model.drive.FilePropertyDataSource
import jp.panta.misskeyandroidclient.model.drive.InMemoryFilePropertyDataSource

@Module
@InstallIn(SingletonComponent::class)
abstract class DriveFileModule {
    @Binds
    abstract fun filePropertyDataSource(inMem: InMemoryFilePropertyDataSource): FilePropertyDataSource

    @Binds
    abstract fun driveFileRepository(repo: DriveFileRepositoryImpl): DriveFileRepository
}