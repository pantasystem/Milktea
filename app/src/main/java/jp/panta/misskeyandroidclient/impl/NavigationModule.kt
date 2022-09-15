package jp.panta.misskeyandroidclient.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import jp.panta.misskeyandroidclient.AntennaNavigationImpl
import net.pantasystem.milktea.userlist.UserListNavigationImpl
import net.pantasystem.milktea.common_navigation.AntennaNavigation
import net.pantasystem.milktea.common_navigation.UserListNavigation

@Module
@InstallIn(ActivityComponent::class)
abstract class NavigationModule {

    @Binds
    abstract fun bindUserListNavigation(impl: net.pantasystem.milktea.userlist.UserListNavigationImpl) : UserListNavigation

    @Binds
    abstract fun bindAntennaNavigation(impl: AntennaNavigationImpl) : AntennaNavigation

}