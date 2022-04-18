package net.pantasystem.milktea.data.infrastructure

import net.pantasystem.milktea.model.account.page.Pageable
import java.io.Serializable

@Deprecated("model.account.pagesへ移行")
interface PageableOld : Serializable{
    val type: PageType

    fun toPageable(): Pageable
}