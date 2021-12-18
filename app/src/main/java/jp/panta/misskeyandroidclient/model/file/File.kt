package jp.panta.misskeyandroidclient.model.file

import jp.panta.misskeyandroidclient.model.drive.FileProperty
import java.io.Serializable

sealed interface AppFile {

    data class Local(
        val name: String,
        val path: String,
        val type: String,
        val thumbnailUrl: String?,
        val isSensitive: Boolean,
        val folderId: String?,
        val id: Long = 0,
        ) : AppFile
    data class Remote(
        val id: FileProperty.Id,
    ) : AppFile

}

data class File(
    val name: String,
    val path: String?,
    val type: String?,
    val remoteFileId: FileProperty.Id?,
    val localFileId: Long?,
    val thumbnailUrl: String?,
    val isSensitive: Boolean?,
    val folderId: String? = null
): Serializable {
    val isRemoteFile: Boolean
        get() = remoteFileId != null
}


fun AppFile.toFile(): File {
    return when(this) {
        is AppFile.Remote -> {
           File(
               name = "remote file",
               path = null,
               type = null,
               remoteFileId = id,
               thumbnailUrl = null,
               isSensitive = null,
               folderId = null,
               localFileId = null
           )
        }
        is AppFile.Local -> {
            File(
                name = name,
                path = path,
                type = type,
                remoteFileId = null,
                thumbnailUrl = thumbnailUrl,
                isSensitive = isSensitive,
                folderId = folderId,
                localFileId = id
            )
        }
    }
}