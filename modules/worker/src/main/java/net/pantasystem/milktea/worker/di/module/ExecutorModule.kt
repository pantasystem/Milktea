package net.pantasystem.milktea.worker.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.model.instance.SyncMetaExecutor
import net.pantasystem.milktea.worker.meta.SyncMetaExecutorImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class ExecutorBindModule {

    @Binds
    @Singleton
    abstract fun bindSyncMetaExecutor(impl: SyncMetaExecutorImpl): SyncMetaExecutor
}