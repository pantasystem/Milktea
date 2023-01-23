package net.pantasystem.milktea.api.misskey.drive

import kotlinx.serialization.Serializable

@Serializable
data class DeleteFileDTO(
    val i: String,
    val fileId: String
)