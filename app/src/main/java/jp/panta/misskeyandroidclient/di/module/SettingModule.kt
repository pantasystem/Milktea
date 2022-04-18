package jp.panta.misskeyandroidclient.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.infrastructure.settings.SettingStore
import net.pantasystem.milktea.common.getPreferences
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SettingModule {

    @Singleton
    @Provides
    fun settingStore(@ApplicationContext context: Context): SettingStore {
        return SettingStore(context.getPreferences())
    }
}