package net.pantasystem.milktea.api.misskey.drive

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShowFile(
    @SerialName("i")
    val i: String,

    @SerialName("fileId")
    val fileId: String?,

    @SerialName("url")
    val url: String?
) {
  constructor(fileId: String, i: String) : this(fileId = fileId, i = i, url = null)
}