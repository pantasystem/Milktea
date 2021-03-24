package jp.panta.misskeyandroidclient.model.drive

import kotlinx.serialization.Serializable

@Serializable
data class FolderProperty(
    val id: String,
    val createdAt: String,
    val name: String,
    val foldersCount: Int,
    val filesCount: Int,
    val parentId: String?,
    val parent: FolderProperty?
)