package net.pantasystem.milktea.data.model

import net.pantasystem.milktea.model.account.page.Pageable
import java.io.Serializable

@Deprecated("model.account.pagesへ移行")
interface Pageable : Serializable{
    val type: PageType

    fun toPageable(): net.pantasystem.milktea.model.account.page.Pageable
}