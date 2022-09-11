package net.pantasystem.milktea.media.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import net.pantasystem.milktea.common_navigation.MediaNavigation
import net.pantasystem.milktea.media.MediaNavigationImpl

@Module
@InstallIn(ActivityComponent::class)
abstract class NavigationModule {

    @Binds
    abstract fun bindMediaNavigation(impl: MediaNavigationImpl): MediaNavigation

}