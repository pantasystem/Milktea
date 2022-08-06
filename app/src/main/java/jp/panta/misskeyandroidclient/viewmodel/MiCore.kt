package jp.panta.misskeyandroidclient.viewmodel

import net.pantasystem.milktea.data.infrastructure.url.UrlPreviewStore
import net.pantasystem.milktea.model.account.Account

interface MiCore {
    fun getUrlPreviewStore(account: Account): UrlPreviewStore?
}