package net.pantasystem.milktea.api.misskey.drive

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateFolder(
    @SerialName("i")
    val i: String,

    @SerialName("name")
    val name: String,

    @SerialName("parentId")
    val parentId: String?
)