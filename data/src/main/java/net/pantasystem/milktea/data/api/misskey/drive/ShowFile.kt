package net.pantasystem.milktea.data.api.misskey.drive

data class ShowFile(
    val i: String,
    val fileId: String?,
    val url: String?
) {
  constructor(fileId: String, i: String) : this(fileId = fileId, i = i, url = null)
}