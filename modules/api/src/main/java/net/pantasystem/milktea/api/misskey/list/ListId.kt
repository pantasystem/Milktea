package net.pantasystem.milktea.api.misskey.list

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ListId (
    @SerialName("i")
    val i: String,

    @SerialName("listId")
    val listId: String
)