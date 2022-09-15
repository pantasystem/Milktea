package jp.panta.misskeyandroidclient.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import jp.panta.misskeyandroidclient.MainNavigationImpl
import net.pantasystem.milktea.user.activity.SearchAndSelectUserNavigationImpl
import jp.panta.misskeyandroidclient.SearchNavigationImpl
import net.pantasystem.milktea.user.activity.UserDetailNavigationImpl
import net.pantasystem.milktea.common_navigation.MainNavigation
import net.pantasystem.milktea.common_navigation.SearchAndSelectUserNavigation
import net.pantasystem.milktea.common_navigation.SearchNavigation
import net.pantasystem.milktea.common_navigation.UserDetailNavigation

@Module
@InstallIn(ActivityComponent::class)
abstract class NavigationModule {

    @Binds
    abstract fun provideUserDetailNavigation(impl: UserDetailNavigationImpl): UserDetailNavigation

    @Binds
    abstract fun bindMainNavigation(impl: MainNavigationImpl): MainNavigation

    @Binds
    abstract fun bindSearchAndSelectUserNavigation(impl: SearchAndSelectUserNavigationImpl): SearchAndSelectUserNavigation

    @Binds
    abstract fun bindSearchResultNavigation(impl: SearchNavigationImpl): SearchNavigation
}