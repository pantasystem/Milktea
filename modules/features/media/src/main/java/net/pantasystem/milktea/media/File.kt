package net.pantasystem.milktea.media

import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.file.AboutMediaType
import net.pantasystem.milktea.model.file.AppFile
import java.io.Serializable


data class File(
    val name: String,
    val path: String?,
    val type: String?,
    val remoteFileId: FileProperty.Id?,
    val localFileId: Long?,
    val thumbnailUrl: String?,
    val isSensitive: Boolean?,
    val folderId: String? = null,
    val comment: String? = null,
    val blurhash: String? = null,
) : Serializable {

    companion object {
        fun from(fileProperty: FileProperty): File {
            return with(fileProperty) {
                File(
                    name,
                    url,
                    type,
                    id,
                    null,
                    thumbnailUrl,
                    isSensitive,
                    null,
                    comment,
                    blurhash = blurhash,
                )
            }

        }
    }


    val aboutMediaType = when {
        this.type == null -> AboutMediaType.OTHER
        this.type.startsWith("image") -> AboutMediaType.IMAGE
        this.type.startsWith("video") -> AboutMediaType.VIDEO
        this.type.startsWith("audio") -> AboutMediaType.SOUND
        else -> AboutMediaType.OTHER
    }
}
fun AppFile.Local.toFile(): File {
    return File(
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
