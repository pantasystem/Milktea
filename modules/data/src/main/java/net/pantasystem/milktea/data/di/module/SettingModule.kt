package net.pantasystem.milktea.data.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common.getPreferences
import net.pantasystem.milktea.model.setting.ColorSettingStore
import net.pantasystem.milktea.data.infrastructure.settings.LocalConfigRepositoryImpl
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.model.setting.NoteExpandedHeightSize
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SettingModule {

    @Singleton
    @Provides
    fun settingStore(@ApplicationContext context: Context, repository: LocalConfigRepository, coroutineScope: CoroutineScope): SettingStore {
        return SettingStore(context.getPreferences(), repository, coroutineScope)
    }

    @Singleton
    @Provides
    fun provideLocalConfigRepository(@ApplicationContext context: Context): LocalConfigRepository {
        return LocalConfigRepositoryImpl(context.getPreferences())
    }

    @Singleton
    @Provides
    fun  provideNoteExpandedHeightSize(settingStore: SettingStore): NoteExpandedHeightSize {
        return settingStore
    }

    @Singleton
    @Provides
    fun provideColorSettingStore(@ApplicationContext context: Context): ColorSettingStore {
        return ColorSettingStore(context.getPreferences())
    }
}