package net.pantasystem.milktea.model.account.page

import android.os.Parcelable
import androidx.room.*
import kotlinx.parcelize.Parcelize
import java.io.Serializable


@Parcelize
data class Page(
    var accountId: Long,
    val title: String,
    var weight: Int,
    val pageParams: PageParams,
    var pageId: Long
) : Serializable, Parcelable {

    constructor(accountId: Long, title: String, weight: Int, pageable: Pageable, pageId: Long = 0)
            : this(accountId, title, weight, pageable.toParams(), pageId)


    @Ignore
    fun pageable(): Pageable {
        return pageParams.toPageable()
    }

    fun isEqualEntity(page: Page): Boolean {
        if (page.accountId != this.accountId) {
            return false
        }
        if (page.pageId == this.pageId) {
            return true
        }
        return page.pageable() == pageable()
    }
}

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
    constructor(accountId: Long, title: String, weight: Int, pageable: Pageable, pageId: Long = 0)
            : this(accountId, title, weight, pageable.toParams(), pageId)

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