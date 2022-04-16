package net.pantasystem.milktea.data.api.misskey.list

import kotlinx.serialization.Serializable

@Serializable
data class UpdateList(
    val i: String,
    val listId: String,
    val name: String
)