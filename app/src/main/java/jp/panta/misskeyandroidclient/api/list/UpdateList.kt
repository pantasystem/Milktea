package jp.panta.misskeyandroidclient.api.list

import kotlinx.serialization.Serializable

@Serializable
data class UpdateList(
    val i: String,
    val listId: String,
    val name: String
)