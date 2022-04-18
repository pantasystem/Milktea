package net.pantasystem.milktea.data.model.account.page.db

import androidx.room.*
import net.pantasystem.milktea.model.account.page.Page

@Dao
interface PageDAO{

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(pages: List<net.pantasystem.milktea.model.account.page.Page>)

    @Update
    fun update(page: net.pantasystem.milktea.model.account.page.Page)

    @Update
    fun updateAll(pages: List<net.pantasystem.milktea.model.account.page.Page>)

    @Delete
    fun deleteAll(pages: List<net.pantasystem.milktea.model.account.page.Page>)

    @Query("delete from page_table where accountId = :accountId")
    fun clearByAccountId(accountId: Long)

    @Query("select * from page_table where accountId = :accountId")
    fun findAllByAccount(accountId: Long): List<net.pantasystem.milktea.model.account.page.Page>

    @Query("select * from page_table where pageId = :pageId")
    fun get(pageId: Long): net.pantasystem.milktea.model.account.page.Page?

}

