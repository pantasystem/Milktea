package jp.panta.misskeyandroidclient.ui

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.pantasystem.milktea.data.infrastructure.settings.SettingStore
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.model.instance.MetaCache


@EntryPoint
@InstallIn(SingletonComponent::class)
interface BindingProvider {
    fun settingStore(): SettingStore
    fun accountStore(): AccountStore
    fun metaCache(): MetaCache
}