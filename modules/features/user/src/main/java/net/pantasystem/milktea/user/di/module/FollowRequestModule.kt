package net.pantasystem.milktea.user.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import net.pantasystem.milktea.common_android_ui.user.FollowRequestsFragmentFactory
import net.pantasystem.milktea.user.follow_requests.FollowRequestFragmentFactoryImpl

@InstallIn(ActivityComponent::class)
@Module
abstract class FollowRequestModule {

    @Binds
    abstract fun bindFollowRequestFragmentFactory(impl: FollowRequestFragmentFactoryImpl): FollowRequestsFragmentFactory
}