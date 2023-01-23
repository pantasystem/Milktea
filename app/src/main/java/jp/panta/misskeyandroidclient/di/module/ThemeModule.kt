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

@Module
@InstallIn(ActivityComponent::class)
object ThemeModule {

    @Provides
    fun provideSetTheme(activity: Activity): ApplyTheme {
        return ApplyThemeImpl(activity)
    }

    @Provides
    fun provideMenuTint(): ApplyMenuTint {
        return ApplyMenuTintImpl()
    }
}