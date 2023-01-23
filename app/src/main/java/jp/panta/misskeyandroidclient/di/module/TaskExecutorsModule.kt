package jp.panta.misskeyandroidclient.di.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.model.CreateGalleryTaskExecutor
import net.pantasystem.milktea.model.TaskExecutorImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object TaskExecutorsModule {

    @Provides
    @Singleton
    fun provideGalleryPostTaskExecutor(
        coroutineScope: CoroutineScope,
        loggerFactory: Logger.Factory,
    ): CreateGalleryTaskExecutor {
        return CreateGalleryTaskExecutor(
            provideTaskExecutor(coroutineScope, loggerFactory)
        )
    }

    private fun <T> provideTaskExecutor(
        coroutineScope: CoroutineScope,
        loggerFactory: Logger.Factory,
    ): TaskExecutorImpl<T> {
        return TaskExecutorImpl(coroutineScope, loggerFactory.create("CreateNoteTaskExecutor"))
    }

}