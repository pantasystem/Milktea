package jp.panta.misskeyandroidclient.model

import java.io.Serializable

interface Pageable : Serializable{
    val type: PageType
}