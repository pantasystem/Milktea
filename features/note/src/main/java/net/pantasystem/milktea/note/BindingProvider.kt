package net.pantasystem.milktea.note

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common_navigation.MediaNavigation
import net.pantasystem.milktea.model.instance.MetaCache



@EntryPoint
@InstallIn(ActivityComponent::class)
interface ActivityBindingProvider {
    fun mediaNavigation(): MediaNavigation
}