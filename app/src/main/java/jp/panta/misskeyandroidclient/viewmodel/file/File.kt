package jp.panta.misskeyandroidclient.viewmodel.file

data class File(
    val name: String,
    val path: String,
    val type: String?,
    val remoteFileId: String?,
    val thumbnailUrl: String?,
    val isSensitive: Boolean?
)