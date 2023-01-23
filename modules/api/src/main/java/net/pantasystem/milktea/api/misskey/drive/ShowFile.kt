package net.pantasystem.milktea.api.misskey.drive

import kotlinx.serialization.Serializable

@Serializable
data class ShowFile(
    val i: String,
    val fileId: String?,
    val url: String?
) {
  constructor(fileId: String, i: String) : this(fileId = fileId, i = i, url = null)
}