package net.pantasystem.milktea.data.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.infrastructure.file.CopyFileToAppDirUseCaseImpl
import net.pantasystem.milktea.model.file.CopyFileToAppDirUseCase
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class FileModule {

    @Singleton
    @Binds
    abstract fun bindCopyFileToUseCase(
        impl: CopyFileToAppDirUseCaseImpl
    ): CopyFileToAppDirUseCase
}