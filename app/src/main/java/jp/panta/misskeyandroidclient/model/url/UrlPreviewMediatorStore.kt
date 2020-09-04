package jp.panta.misskeyandroidclient.model.url

import jp.panta.misskeyandroidclient.model.url.db.UrlPreviewDAO

class UrlPreviewMediatorStore(
    private val urlPreviewDAO: UrlPreviewDAO,
    private val remoteStore: UrlPreviewStore
) : UrlPreviewStore{


    override fun get(url: String): UrlPreview? {
        var preview = urlPreviewDAO.findByUrl(url)
        if(preview == null){
            preview = remoteStore.get(url)?.apply{
                urlPreviewDAO.insert(this)
            }
        }
        return preview

    }
}