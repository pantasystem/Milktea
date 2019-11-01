package jp.panta.misskeyandroidclient.model.drive

data class FolderProperty(
    val id: String,
    val createdAt: String,
    val name: String,
    val foldersCount: Int,
    val filesCount: Int,
    val parentId: String?,
    val parent: FolderProperty?
)