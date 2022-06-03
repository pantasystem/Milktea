package jp.panta.misskeyandroidclient.di.module

import android.app.Activity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import jp.panta.misskeyandroidclient.SetThemeImpl
import net.pantasystem.milktea.common.ui.SetTheme

@Module
@InstallIn(ActivityComponent::class)
object ThemeModule {

    @Provides
    fun provideSetTheme(activity: Activity): SetTheme {
        return SetThemeImpl(activity)
    }
}