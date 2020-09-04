package jp.panta.misskeyandroidclient.model.url.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import jp.panta.misskeyandroidclient.model.url.UrlPreview

@Dao
interface UrlPreviewDAO {
    @Query("select * from url_preview where url = :url")
    fun findByUrl(url: String): UrlPreview?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(urlPreview: UrlPreview)
}