package net.pantasystem.milktea.channel.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import net.pantasystem.milktea.channel.ChannelDetailNavigationImpl
import net.pantasystem.milktea.channel.ChannelNavigationImpl
import net.pantasystem.milktea.common_navigation.ChannelDetailNavigation
import net.pantasystem.milktea.common_navigation.ChannelNavigation

@Module
@InstallIn(ActivityComponent::class)
abstract class NavigationModule {
    @Binds
    abstract fun bindChannelNavigation(impl: ChannelNavigationImpl): ChannelNavigation

    @Binds
    abstract fun bindChannelDetailNavigation(impl: ChannelDetailNavigationImpl): ChannelDetailNavigation
}