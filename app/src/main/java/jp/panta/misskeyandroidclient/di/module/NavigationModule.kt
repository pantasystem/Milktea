package jp.panta.misskeyandroidclient.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import jp.panta.misskeyandroidclient.MainNavigationImpl
import jp.panta.misskeyandroidclient.UserDetailNavigationImpl
import net.pantasystem.milktea.auth.AuthorizationNavigationImpl
import net.pantasystem.milktea.common_navigation.*
import net.pantasystem.milktea.drive.DriveNavigationImpl
import net.pantasystem.milktea.media.MediaNavigationImpl

@Module
@InstallIn(ActivityComponent::class)
abstract class NavigationModule {

    @Binds
    abstract fun providerDriveNavigation(impl: DriveNavigationImpl): DriveNavigation

    @Binds
    abstract fun provideUserDetailNavigation(impl: UserDetailNavigationImpl): UserDetailNavigation

    @Binds
    abstract fun provideAuthorizationNavigation(impl: AuthorizationNavigationImpl): AuthorizationNavigation

    @Binds
    abstract fun bindMediaNavigation(impl: MediaNavigationImpl): MediaNavigation

    @Binds
    abstract fun bindMainNavigation(impl: MainNavigationImpl): MainNavigation
}