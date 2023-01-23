package jp.panta.misskeyandroidclient.di.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import jp.panta.misskeyandroidclient.MainNavigationImpl
import net.pantasystem.milktea.common_navigation.*
import net.pantasystem.milktea.search.SearchNavigationImpl
import net.pantasystem.milktea.setting.activities.AccountSettingActivityNavigationImpl
import net.pantasystem.milktea.user.activity.SearchAndSelectUserNavigationImpl
import net.pantasystem.milktea.user.activity.UserDetailNavigationImpl

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

    @Binds
    abstract fun bindAccountSettingNav(impl: AccountSettingActivityNavigationImpl) : AccountSettingNavigation
}