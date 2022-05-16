package jp.panta.misskeyandroidclient.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.common.getPreferences
import net.pantasystem.milktea.data.infrastructure.settings.LocalConfigRepositoryImpl
import net.pantasystem.milktea.data.infrastructure.settings.SettingStore
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SettingModule {

    @Singleton
    @Provides
    fun settingStore(@ApplicationContext context: Context, repository: LocalConfigRepository): SettingStore {
        return SettingStore(context.getPreferences(), repository)
    }

    @Singleton
    @Provides
    fun provideLocalConfigRepository(@ApplicationContext context: Context): LocalConfigRepository {
        return LocalConfigRepositoryImpl(context.getPreferences())
    }
}