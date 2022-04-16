package jp.panta.misskeyandroidclient.di.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.data.model.CreateGalleryTaskExecutor
import net.pantasystem.milktea.data.model.CreateNoteTaskExecutor
import net.pantasystem.milktea.data.model.TaskExecutorImpl
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object TaskExecutorsModule {

    @Provides
    @Singleton
    fun provideNoteCreateTaskExecutor(
        coroutineScope: CoroutineScope,
        loggerFactory: net.pantasystem.milktea.common.Logger.Factory,
    ): CreateNoteTaskExecutor {
        return CreateNoteTaskExecutor(
            provideTaskExecutor(coroutineScope, loggerFactory)
        )
    }

    @Provides
    @Singleton
    fun provideGalleryPostTaskExecutor(
        coroutineScope: CoroutineScope,
        loggerFactory: net.pantasystem.milktea.common.Logger.Factory,
    ): CreateGalleryTaskExecutor {
        return CreateGalleryTaskExecutor(
            provideTaskExecutor(coroutineScope, loggerFactory)
        )
    }

    private fun <T> provideTaskExecutor(
        coroutineScope: CoroutineScope,
        loggerFactory: net.pantasystem.milktea.common.Logger.Factory,
    ): TaskExecutorImpl<T> {
        return TaskExecutorImpl(coroutineScope, loggerFactory.create("CreateNoteTaskExecutor"))
    }

}