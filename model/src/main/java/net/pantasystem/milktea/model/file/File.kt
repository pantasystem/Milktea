package net.pantasystem.milktea.model.file

import net.pantasystem.milktea.model.drive.FileProperty
import java.io.Serializable as JSerializable

sealed interface AppFile : JSerializable {

    data class Local(
        val name: String,
        val path: String,
        val type: String,
        val thumbnailUrl: String?,
        val isSensitive: Boolean,
        val folderId: String?,
        val id: Long = 0,
    ) : AppFile {
        fun isAttributeSame(file: Local): Boolean {
            return file.name == name
                    && file.path == path
                    && file.type == type
        }
    }

    data class Remote(
        val id: net.pantasystem.milktea.model.drive.FileProperty.Id,
    ) : AppFile

}


data class File(
    val name: String,
    val path: String?,
    val type: String?,
    val remoteFileId: net.pantasystem.milktea.model.drive.FileProperty.Id?,
    val localFileId: Long?,
    val thumbnailUrl: String?,
    val isSensitive: Boolean?,
    val folderId: String? = null
) : JSerializable {
    enum class AboutMediaType {
        VIDEO, IMAGE, SOUND, OTHER

    }

    val isRemoteFile: Boolean
        get() = remoteFileId != null

    val aboutMediaType = when {
        this.type == null -> AboutMediaType.OTHER
        this.type.startsWith("image") -> AboutMediaType.IMAGE
        this.type.startsWith("video") -> AboutMediaType.VIDEO
        this.type.startsWith("audio") -> AboutMediaType.SOUND
        else -> AboutMediaType.OTHER
    }
}


fun AppFile.toFile(): File {
    return when (this) {
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