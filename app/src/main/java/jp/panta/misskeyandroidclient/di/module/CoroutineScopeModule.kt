package jp.panta.misskeyandroidclient.di.module

import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object CoroutinesScopesModule {

    @Singleton // Provide always the same instance
    @Provides
    fun providesCoroutineScope(): CoroutineScope {
        val errorHandler = CoroutineExceptionHandler { _, throwable ->
            FirebaseCrashlytics.getInstance().recordException(throwable)
        }
        return CoroutineScope(SupervisorJob() + Dispatchers.Default + errorHandler)
    }
}