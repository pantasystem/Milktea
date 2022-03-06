package jp.panta.misskeyandroidclient.api.misskey.drive

data class UpdateFileDTO (
    val i: String,
    val fileId: String,
    val folderId: String?,
    val name: String,
    val comment: String?,
    val isSensitive: Boolean,
)