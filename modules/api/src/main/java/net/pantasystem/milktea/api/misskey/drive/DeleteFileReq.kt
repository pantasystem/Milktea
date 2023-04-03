package net.pantasystem.milktea.api.misskey.drive

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeleteFileDTO(
    @SerialName("i")
    val i: String,

    @SerialName("fileId")
    val fileId: String
)