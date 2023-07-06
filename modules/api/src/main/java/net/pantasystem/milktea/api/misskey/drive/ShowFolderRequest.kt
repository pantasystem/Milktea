package net.pantasystem.milktea.api.misskey.drive

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShowFolderRequest(
    @SerialName("i") val i: String,
    @SerialName("folderId") val folderId: String,
)