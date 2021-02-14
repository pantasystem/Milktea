package jp.panta.misskeyandroidclient.model.list

import kotlinx.serialization.Serializable

@Serializable
data class ListUserOperation(
    val i: String,
    val listId: String,
    val userId: String
)