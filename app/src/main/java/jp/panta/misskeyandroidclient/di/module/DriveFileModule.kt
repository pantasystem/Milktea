package jp.panta.misskeyandroidclient.di.module

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.common.GsonFactory
import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.data.infrastructure.drive.*
import okhttp3.OkHttpClient
import javax.inject.Singleton
import net.pantasystem.milktea.model.drive.InMemoryFilePropertyDataSource
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.drive.DriveFileRepository
@Module
@InstallIn(SingletonComponent::class)
abstract class DriveFileBindModule {
    @Binds
    @Singleton
    abstract fun filePropertyDataSource(inMem: InMemoryFilePropertyDataSource): FilePropertyDataSource

    @Binds
    @Singleton
    abstract fun driveFileRepository(repo: DriveFileRepositoryImpl): DriveFileRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DriveFileModule {
    @Provides
    @Singleton
    fun uploader(@ApplicationContext context: Context, encryption: Encryption) : FileUploaderProvider {
        return OkHttpFileUploaderProvider(
            OkHttpClient(),
            context,
            GsonFactory.create(),
            encryption
        )
    }
}