package net.pantasystem.milktea.data.api.misskey.list

import kotlinx.serialization.Serializable

@Serializable
data class ListUserOperation(
    val i: String,
    val listId: String,
    val userId: String
)