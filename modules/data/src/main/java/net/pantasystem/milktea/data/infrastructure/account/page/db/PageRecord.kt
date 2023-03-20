package net.pantasystem.milktea.data.infrastructure.account.page.db

import androidx.room.*
import net.pantasystem.milktea.model.account.page.Page

@Entity(
    tableName = "page_table",
    indices = [Index("weight"), Index("accountId")]
)
data class PageRecord(
    @ColumnInfo(name = "accountId")
    var accountId: Long,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "weight")
    var weight: Int,

    @Embedded val pageParams: PageRecordParams,

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "pageId")
    var pageId: Long
) {

    companion object {
        fun from(page: Page): PageRecord {
            return PageRecord(
                accountId = page.accountId,
                title = page.title,
                weight = page.weight,
                pageParams = PageRecordParams.from(page.pageParams),
                pageId = page.pageId
            )
        }
    }

    fun toPage(): Page {
        return Page(
            accountId = accountId,
            title = title,
            weight = weight,
            pageParams = pageParams.toParams(),
            pageId = pageId
        )
    }
}