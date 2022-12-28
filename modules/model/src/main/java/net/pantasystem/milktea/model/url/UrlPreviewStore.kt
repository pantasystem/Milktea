package net.pantasystem.milktea.model.url

import net.pantasystem.milktea.model.account.Account

interface UrlPreviewStore{

    fun get(url: String): UrlPreview?
}

interface UrlPreviewStoreProvider {
    fun getUrlPreviewStore(account: Account): UrlPreviewStore

    fun getUrlPreviewStore(
        account: Account,
        isReplace: Boolean
    ): UrlPreviewStore
}