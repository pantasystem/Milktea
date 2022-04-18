package net.pantasystem.milktea.data.infrastructure.url

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import net.pantasystem.milktea.data.infrastructure.url.db.UrlPreviewDAO

class UrlPreviewMediatorStore(
    private val urlPreviewDAO: UrlPreviewDAO,
    private val remoteStore: UrlPreviewStore
) : UrlPreviewStore{


    override fun get(url: String): UrlPreview? {
        var preview = urlPreviewDAO.findByUrl(url)
        if(preview == null){
            preview = remoteStore.get(url)?.apply{
                try{

                    urlPreviewDAO.insert(this)
                }catch(e: SQLiteConstraintException){
                    Log.w("UrlPreviewMediatorStore", "不正なデータ", e)
                }
            }
        }
        return preview

    }
}