package jp.panta.misskeyandroidclient.di.module

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import net.pantasystem.milktea.api.misskey.OkHttpClientProvider
import net.pantasystem.milktea.app_store.drive.FilePropertyPagingStore
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIFactory
import net.pantasystem.milktea.data.infrastructure.drive.*
import net.pantasystem.milktea.model.drive.DriveFileRepository
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DriveFileBindModule {
    @Binds
    @Singleton
    abstract fun filePropertyDataSource(inMem: MediatorFilePropertyDataSource): FilePropertyDataSource

    @Binds
    @Singleton
    abstract fun driveFileRepository(repo: DriveFileRepositoryImpl): DriveFileRepository

    @Binds
    @Singleton
    abstract fun provideFilePropertyPagingStore(impl: FilePropertyPagingStoreImpl): FilePropertyPagingStore
}

@Module
@InstallIn(SingletonComponent::class)
object DriveFileModule {
    @Provides
    @Singleton
    fun uploader(
        @ApplicationContext context: Context,
        okHttpClientProvider: OkHttpClientProvider,
        filePropertyDataSource: FilePropertyDataSource,
        mastodonAPIFactory: MastodonAPIFactory,
    ): FileUploaderProvider {
        return OkHttpFileUploaderProvider(
            okHttpClientProvider,
            context,
            json = Json {
                ignoreUnknownKeys = true
            },
            filePropertyDataSource = filePropertyDataSource,
            mastodonAPIFactory = mastodonAPIFactory,
        )
    }
}