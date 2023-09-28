package jp.panta.misskeyandroidclient.di.module

import android.app.Activity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import jp.panta.misskeyandroidclient.ApplyMenuTintImpl
import jp.panta.misskeyandroidclient.ApplyThemeImpl
import net.pantasystem.milktea.common.ui.ApplyMenuTint
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.model.setting.LocalConfigRepository

@Module
@InstallIn(ActivityComponent::class)
object ThemeModule {

    @Provides
    fun provideSetTheme(activity: Activity, configRepository: LocalConfigRepository): ApplyTheme {
        return ApplyThemeImpl(activity, configRepository)
    }

    @Provides
    fun provideMenuTint(): ApplyMenuTint {
        return ApplyMenuTintImpl()
    }
}