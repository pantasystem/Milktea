package jp.panta.misskeyandroidclient.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import net.pantasystem.milktea.antenna.AntennaNavigationImpl
import net.pantasystem.milktea.common_navigation.AntennaNavigation
import net.pantasystem.milktea.common_navigation.UserListNavigation
import net.pantasystem.milktea.userlist.UserListNavigationImpl

@Module
@InstallIn(ActivityComponent::class)
abstract class NavigationModule {

    @Binds
    abstract fun bindUserListNavigation(impl: UserListNavigationImpl) : UserListNavigation

    @Binds
    abstract fun bindAntennaNavigation(impl: AntennaNavigationImpl) : AntennaNavigation

}