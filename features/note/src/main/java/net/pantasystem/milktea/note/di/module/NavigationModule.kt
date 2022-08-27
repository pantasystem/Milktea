package net.pantasystem.milktea.note.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import net.pantasystem.milktea.common_navigation.TimeMachineNavigation
import net.pantasystem.milktea.note.TimeMachineNavigationImpl

@Module
@InstallIn(ActivityComponent::class)
abstract class NavigationModule {
    @Binds
    abstract fun provideTimeMachineNavigation(impl: TimeMachineNavigationImpl): TimeMachineNavigation
}