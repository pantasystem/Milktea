package net.pantasystem.milktea.api.misskey.list

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateList(
    @SerialName("i")
    val i: String,

    @SerialName("listId")
    val listId: String,

    @SerialName("name")
    val name: String
)