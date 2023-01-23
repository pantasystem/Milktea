package net.pantasystem.milktea.messaging.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import net.pantasystem.milktea.common_navigation.MessageNavigation
import net.pantasystem.milktea.messaging.MessageNavigationImpl

@Module
@InstallIn(ActivityComponent::class)
abstract class NavigationModule {
    @Binds
    abstract fun bindMessageActivityNavigation(impl: MessageNavigationImpl): MessageNavigation
}