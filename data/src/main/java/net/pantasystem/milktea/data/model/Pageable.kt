package net.pantasystem.milktea.data.model

import net.pantasystem.milktea.data.model.account.page.Pageable
import java.io.Serializable

@Deprecated("model.account.pagesへ移行")
interface Pageable : Serializable{
    val type: PageType

    fun toPageable(): Pageable
}