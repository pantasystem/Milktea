package net.pantasystem.milktea.data.infrastructure.url

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import net.pantasystem.milktea.data.infrastructure.url.db.UrlPreviewDAO
import net.pantasystem.milktea.model.url.UrlPreview
import net.pantasystem.milktea.data.infrastructure.url.db.UrlPreviewRecord
import net.pantasystem.milktea.model.url.UrlPreviewStore

class UrlPreviewMediatorStore(
    private val urlPreviewDAO: UrlPreviewDAO,
    private val remoteStore: UrlPreviewStore
) : UrlPreviewStore {


    override fun get(url: String): UrlPreview? {
        var preview = urlPreviewDAO.findByUrl(url)?.toModel()
        if(preview == null){
            preview = remoteStore.get(url)?.apply{
                try{
                    urlPreviewDAO.insert(UrlPreviewRecord.from(this))
                }catch(e: SQLiteConstraintException){
                    Log.w("UrlPreviewMediatorStore", "不正なデータ", e)
                }
            }
        }
        return preview

    }
}