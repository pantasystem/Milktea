package jp.panta.misskeyandroidclient.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.util.DebuggerSetupManager
import jp.panta.misskeyandroidclient.util.EmptyDebuggerSetupManagerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReleaseAppModule {

    @Binds
    @Singleton
    abstract fun provideFlipperSetupManager(manager: EmptyDebuggerSetupManagerImpl): DebuggerSetupManager
}