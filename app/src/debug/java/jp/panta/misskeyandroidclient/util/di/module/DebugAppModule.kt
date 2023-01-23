package jp.panta.misskeyandroidclient.util.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.util.DebuggerSetupManager
import jp.panta.misskeyandroidclient.util.FlipperSetupManagerImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class DebugAppModule {

    @Binds
    @Singleton
    abstract fun bindFlipperSetupManager(impl: FlipperSetupManagerImpl): DebuggerSetupManager
}