package net.pantasystem.milktea.api.misskey.list

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateList(
    @SerialName("i")
    val i: String,

    @SerialName("name")
    val name: String
)