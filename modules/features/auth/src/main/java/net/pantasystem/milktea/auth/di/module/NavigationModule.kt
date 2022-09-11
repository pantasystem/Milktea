package net.pantasystem.milktea.auth.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import net.pantasystem.milktea.auth.AuthorizationNavigationImpl
import net.pantasystem.milktea.common_navigation.AuthorizationNavigation


@Module
@InstallIn(ActivityComponent::class)
abstract class NavigationModule {
    @Binds
    abstract fun provideAuthorizationNavigation(impl: AuthorizationNavigationImpl): AuthorizationNavigation
}