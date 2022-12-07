package net.pantasystem.milktea.data.infrastructure.account.page.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.account.page.PageParams

@Entity(
    tableName = "page_table",
    indices = [Index("weight"), Index("accountId")]
)
data class PageRecord(
    var accountId: Long,
    val title: String,
    var weight: Int,
    @Embedded val pageParams: PageParams,
    @PrimaryKey(autoGenerate = true) var pageId: Long
) {

    companion object {
        fun from(page: Page): PageRecord {
            return PageRecord(
                accountId = page.accountId,
                title = page.title,
                weight = page.weight,
                pageParams = page.pageParams,
                pageId = page.pageId
            )
        }
    }

    fun toPage(): Page {
        return Page(
            accountId = accountId,
            title = title,
            weight = weight,
            pageParams = pageParams,
            pageId = pageId
        )
    }
}