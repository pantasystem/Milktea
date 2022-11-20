package net.pantasystem.milktea.common_android_ui

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common_navigation.MediaNavigation
import net.pantasystem.milktea.common_navigation.SearchNavigation
import net.pantasystem.milktea.common_navigation.UserDetailNavigation
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.model.setting.ColorSettingStore


@EntryPoint
@InstallIn(ActivityComponent::class)
interface NavigationEntryPointForBinding {
    fun mediaNavigation(): MediaNavigation
    fun userDetailNavigation(): UserDetailNavigation
    fun searchNavigation(): SearchNavigation
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface BindingProvider {
    fun settingStore(): SettingStore
    fun accountStore(): AccountStore
    fun metaRepository(): MetaRepository
    fun colorSettingStore(): ColorSettingStore
}