package net.pantasystem.milktea.clip

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import net.pantasystem.milktea.common_navigation.ClipDetailNavigation
import net.pantasystem.milktea.common_navigation.ClipListNavigation

@Module
@InstallIn(ActivityComponent::class)
abstract class NavigationModule {

    @Binds
    abstract fun bindClipListNavigation(impl: ClipListNavigationImpl): ClipListNavigation

    @Binds
    abstract fun bindClipDetailNavigation(impl: ClipDetailNavigationImpl): ClipDetailNavigation

}