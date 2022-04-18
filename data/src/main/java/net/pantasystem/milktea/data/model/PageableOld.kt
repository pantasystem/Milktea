package net.pantasystem.milktea.data.model

import java.io.Serializable

@Deprecated("model.account.pagesへ移行")
interface PageableOld : Serializable{
    val type: PageType

    fun toPageable(): net.pantasystem.milktea.model.account.page.Pageable
}