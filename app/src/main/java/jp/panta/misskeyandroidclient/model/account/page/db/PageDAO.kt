package jp.panta.misskeyandroidclient.model.account.page.db

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import jp.panta.misskeyandroidclient.model.account.page.Page

interface PageDAO{

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(pages: List<Page>)

    @Update
    fun update(page: Page)

    @Query("delete from page_table where accountId = :accountId")
    fun clearByAccountId(accountId: Long)

    @Query("select * from page_table where accountId = :accountId")
    fun findAllByAccount(accountId: Long): List<Page>

    @Query("select * from page_table where pageId = :pageId")
    fun get(pageId: Long): Page?
}

