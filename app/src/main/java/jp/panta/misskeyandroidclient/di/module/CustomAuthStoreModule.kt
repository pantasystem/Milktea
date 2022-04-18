package jp.panta.misskeyandroidclient.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.infrastructure.auth.custom.CustomAuthStore
import net.pantasystem.milktea.common.getPreferences

@Module
@InstallIn(SingletonComponent::class)
object CustomAuthStoreModule {

    @Provides
    fun provideCustomAuthStore(
        @ApplicationContext context: Context
    ): CustomAuthStore {
        return CustomAuthStore(context.getPreferences())
    }
}