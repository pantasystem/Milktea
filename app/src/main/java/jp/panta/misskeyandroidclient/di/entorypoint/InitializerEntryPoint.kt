package jp.panta.misskeyandroidclient.di.entorypoint

import android.content.Context
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.util.DebuggerSetupManager

@EntryPoint
@InstallIn(SingletonComponent::class)
interface InitializerEntryPoint {

    companion object {
        fun resolve(context: Context): InitializerEntryPoint {
            val appContext = context.applicationContext ?: throw IllegalStateException()
            return EntryPointAccessors.fromApplication(
                appContext,
                InitializerEntryPoint::class.java
            )
        }
    }

    fun debuggerSetupManager(): DebuggerSetupManager
}