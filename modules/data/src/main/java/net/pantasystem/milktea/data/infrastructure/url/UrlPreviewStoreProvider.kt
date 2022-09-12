package net.pantasystem.milktea.data.infrastructure.url

import net.pantasystem.milktea.api.misskey.MisskeyAPIServiceBuilder
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.data.infrastructure.url.db.UrlPreviewDAO
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.url.UrlPreviewStore
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UrlPreviewStoreProvider @Inject constructor(
    val settingStore: SettingStore,
    val urlPreviewDAO: UrlPreviewDAO,
    val accountStore: AccountStore,
    private val misskeyAPIServiceBuilder: MisskeyAPIServiceBuilder,

    ) {

    private val mUrlPreviewStoreInstanceBaseUrlMap = ConcurrentHashMap<String, UrlPreviewStore>()

    fun getUrlPreviewStore(account: Account): UrlPreviewStore {
        return getUrlPreviewStore(account, false)
    }

    fun getUrlPreviewStore(
        account: Account,
        isReplace: Boolean
    ): UrlPreviewStore {
        return account.instanceDomain.let { accountUrl ->
            val url = settingStore.urlPreviewSetting.getSummalyUrl() ?: accountUrl

            var store = mUrlPreviewStoreInstanceBaseUrlMap[url]
            if (store == null || isReplace) {
                store = UrlPreviewStoreFactory(
                    urlPreviewDAO, settingStore.urlPreviewSetting.getSourceType(),
                    settingStore.urlPreviewSetting.getSummalyUrl(),
                    accountStore.state.value.currentAccount,
                    misskeyAPIServiceBuilder
                ).create()
            }
            mUrlPreviewStoreInstanceBaseUrlMap[url] = store
            store
        }
    }
}