package jp.panta.misskeyandroidclient.api.list

import kotlinx.serialization.Serializable

@Serializable
data class ListId (
    val i: String,
    val listId: String
)