package net.pantasystem.milktea.data.infrastructure.url

import net.pantasystem.milktea.api.misskey.MisskeyAPIServiceBuilder
import net.pantasystem.milktea.api.misskey.OkHttpClientProvider
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.data.infrastructure.url.db.UrlPreviewDAO
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.url.UrlPreviewStore
import net.pantasystem.milktea.model.url.UrlPreviewStoreProvider
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UrlPreviewStoreProviderImpl @Inject constructor(
    val settingStore: SettingStore,
    val urlPreviewDAO: UrlPreviewDAO,
    val accountStore: AccountStore,
    private val misskeyAPIServiceBuilder: MisskeyAPIServiceBuilder,
    private val okHttpClientProvider: OkHttpClientProvider,
    ) : UrlPreviewStoreProvider{

    private val mUrlPreviewStoreInstanceBaseUrlMap = ConcurrentHashMap<String, UrlPreviewStore>()

    override fun getUrlPreviewStore(account: Account): UrlPreviewStore {
        return getUrlPreviewStore(account, false)
    }

    override fun getUrlPreviewStore(
        account: Account,
        isReplace: Boolean
    ): UrlPreviewStore {
        return account.normalizedInstanceUri.let { accountUrl ->

            var store = mUrlPreviewStoreInstanceBaseUrlMap[accountUrl]
            if (store == null || isReplace) {
                store = UrlPreviewStoreFactory(
                    urlPreviewDAO,
                    accountUrl,
                    accountStore.state.value.currentAccount,
                    misskeyAPIServiceBuilder,
                    okHttpClientProvider,
                ).create()
            }
            mUrlPreviewStoreInstanceBaseUrlMap[accountUrl] = store
            store
        }
    }
}