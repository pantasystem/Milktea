package net.pantasystem.milktea.api.misskey.drive

import kotlinx.serialization.SerialName

data class ShowFolderRequest(
    @SerialName("i") val i: String,
    @SerialName("folderId") val folderId: String,
)