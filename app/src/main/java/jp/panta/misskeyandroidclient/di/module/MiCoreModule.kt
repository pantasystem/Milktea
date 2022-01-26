package jp.panta.misskeyandroidclient.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object MiCoreModule {
    @Singleton
    @Provides
    fun provideMiCore(
        @ApplicationContext context: Context
    ) : MiCore {
        return context as MiCore
    }

}