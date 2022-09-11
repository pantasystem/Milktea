package net.pantasystem.milktea.api.misskey.list

import kotlinx.serialization.Serializable

@Serializable
data class UpdateList(
    val i: String,
    val listId: String,
    val name: String
)