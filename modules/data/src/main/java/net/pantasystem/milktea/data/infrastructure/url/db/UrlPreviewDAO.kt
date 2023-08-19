package net.pantasystem.milktea.data.infrastructure.url.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UrlPreviewDAO {
    @Query("select * from url_preview where url = :url and createdAt > datetime('now', '-7 day')")
    fun findByUrl(url: String): UrlPreviewRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(urlPreview: UrlPreviewRecord)
}