package net.pantasystem.milktea.model.account.page

import android.os.Parcelable
import androidx.room.Ignore
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

