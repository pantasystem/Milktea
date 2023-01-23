package net.pantasystem.milktea.drive.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import net.pantasystem.milktea.common_navigation.DriveNavigation
import net.pantasystem.milktea.drive.DriveNavigationImpl

@Module
@InstallIn(ActivityComponent::class)
abstract class NavigationModule {

    @Binds
    abstract fun providerDriveNavigation(impl: DriveNavigationImpl): DriveNavigation

}