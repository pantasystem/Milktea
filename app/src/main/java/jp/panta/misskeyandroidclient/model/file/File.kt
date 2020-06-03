package jp.panta.misskeyandroidclient.model.file

data class File(
    val name: String,
    val path: String,
    val type: String?,
    val remoteFileId: String?,
    val localFileId: Long?,
    val thumbnailUrl: String?,
    val isSensitive: Boolean?
)