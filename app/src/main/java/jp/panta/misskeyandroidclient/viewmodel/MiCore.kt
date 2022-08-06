package jp.panta.misskeyandroidclient.viewmodel

import net.pantasystem.milktea.data.infrastructure.settings.SettingStore
import net.pantasystem.milktea.data.infrastructure.sw.register.SubscriptionRegistration
import net.pantasystem.milktea.data.infrastructure.url.UrlPreviewStore
import net.pantasystem.milktea.model.account.Account

interface MiCore {
    fun getUrlPreviewStore(account: Account): UrlPreviewStore?
    fun getSubscriptionRegistration(): SubscriptionRegistration
    fun getSettingStore(): SettingStore
}