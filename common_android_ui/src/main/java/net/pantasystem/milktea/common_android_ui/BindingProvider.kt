package net.pantasystem.milktea.common_android_ui

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import net.pantasystem.milktea.common_navigation.MediaNavigation
import net.pantasystem.milktea.common_navigation.UserDetailNavigation


@EntryPoint
@InstallIn(ActivityComponent::class)
interface NavigationEntryPointForBinding {
    fun mediaNavigation(): MediaNavigation
    fun userDetailNavigation(): UserDetailNavigation
}