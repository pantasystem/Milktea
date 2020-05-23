package jp.panta.misskeyandroidclient.model.url

interface UrlPreviewStore{

    fun get(url: String): UrlPreview?
}