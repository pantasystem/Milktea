package jp.panta.misskeyandroidclient.model.file

import java.io.Serializable

data class File(
    val name: String,
    val path: String,
    val type: String?,
    val remoteFileId: String?,
    val localFileId: Long?,
    val thumbnailUrl: String?,
    val isSensitive: Boolean?,
    val folderId: String? = null
): Serializable