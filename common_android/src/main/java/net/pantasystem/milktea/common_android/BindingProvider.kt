package net.pantasystem.milktea.common_android

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.model.instance.MetaCache


@EntryPoint
@InstallIn(SingletonComponent::class)
interface BindingProvider {
    fun settingStore(): SettingStore
    fun accountStore(): AccountStore
    fun metaCache(): MetaCache
}