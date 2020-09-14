package jp.panta.misskeyandroidclient.model

import jp.panta.misskeyandroidclient.model.account.page.Pageable
import java.io.Serializable

interface Pageable : Serializable{
    val type: PageType

    fun toPageable(): Pageable
}