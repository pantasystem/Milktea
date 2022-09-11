package net.pantasystem.milktea.api.misskey.list

import kotlinx.serialization.Serializable

@Serializable
data class ListId (
    val i: String,
    val listId: String
)