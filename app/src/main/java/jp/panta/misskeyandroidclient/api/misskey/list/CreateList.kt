package jp.panta.misskeyandroidclient.api.misskey.list

import kotlinx.serialization.Serializable

@Serializable
data class CreateList(
    val i: String,
    val name: String
)