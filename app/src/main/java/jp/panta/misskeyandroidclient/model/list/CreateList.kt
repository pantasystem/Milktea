package jp.panta.misskeyandroidclient.model.list

import kotlinx.serialization.Serializable

@Serializable
data class CreateList(
    val i: String,
    val name: String
)