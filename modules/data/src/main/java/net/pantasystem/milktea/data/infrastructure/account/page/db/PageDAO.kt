package net.pantasystem.milktea.data.infrastructure.account.page.db

import androidx.room.*

@Dao
interface PageDAO{

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(pages: List<PageRecord>)

    @Update
    suspend fun update(page: PageRecord)

    @Update
    suspend fun updateAll(pages: List<PageRecord>)

    @Delete
    suspend fun deleteAll(pages: List<PageRecord>)

    @Query("delete from page_table where accountId = :accountId")
    suspend fun clearByAccountId(accountId: Long)

    @Query("select * from page_table where accountId = :accountId")
    suspend fun findAllByAccount(accountId: Long): List<PageRecord>

    @Query("select * from page_table where pageId = :pageId")
    suspend fun get(pageId: Long): PageRecord?

}

