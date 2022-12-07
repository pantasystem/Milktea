package net.pantasystem.milktea.model.account.page

import android.os.Parcelable
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


    fun pageable(): Pageable {
        return pageParams.toPageable()
    }

}

