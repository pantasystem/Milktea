package net.pantasystem.milktea.model.file

import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.note.draft.DraftNoteFile
import java.io.Serializable as JSerializable

sealed interface AppFile : JSerializable {

    companion object;


    data class Local(
        val name: String,
        val path: String,
        val type: String,
        val thumbnailUrl: String?,
        val isSensitive: Boolean,
        val folderId: String?,
        val fileSize: Long?,
        val comment: String?,
        val id: Long = 0,
    ) : AppFile {
        fun isAttributeSame(file: Local): Boolean {
            return file.name == name
                    && file.path == path
                    && file.type == type
                    && file.fileSize == fileSize
                    && file.comment == comment
        }
    }

    data class Remote(
        val id: FileProperty.Id,
    ) : AppFile

}

enum class AboutMediaType {
    VIDEO, IMAGE, SOUND, OTHER

}

sealed interface FilePreviewSource {
    val file: AppFile

    data class Local(override val file: AppFile.Local) : FilePreviewSource {
        override val type: String = file.type
        override val aboutMediaType: AboutMediaType = when {
            this.type.startsWith("image") -> AboutMediaType.IMAGE
            this.type.startsWith("video") -> AboutMediaType.VIDEO
            this.type.startsWith("audio") -> AboutMediaType.SOUND
            else -> AboutMediaType.OTHER
        }
        override val path: String = file.path
        override val comment: String? = file.comment
        override val name: String = file.name
        override val thumbnailUrl: String? = file.thumbnailUrl
        override val blurhash: String? = null
    }

    data class Remote(override val file: AppFile.Remote, val fileProperty: FileProperty) :
        FilePreviewSource {
        override val type: String = fileProperty.type

        override val aboutMediaType: AboutMediaType = when {
            this.type.startsWith("image") -> AboutMediaType.IMAGE
            this.type.startsWith("video") -> AboutMediaType.VIDEO
            this.type.startsWith("audio") -> AboutMediaType.SOUND
            this.type == "gifv" -> AboutMediaType.VIDEO
            else -> AboutMediaType.OTHER
        }
        override val path: String = fileProperty.url
        override val comment: String? = fileProperty.comment
        override val name: String = fileProperty.name
        override val thumbnailUrl: String? = fileProperty.thumbnailUrl
        override val blurhash: String? = fileProperty.blurhash
    }


    val thumbnailUrl: String?
    val name: String
    val comment: String?

    val type: String
    val path: String
    val aboutMediaType: AboutMediaType
    val blurhash: String?

}

val FilePreviewSource.isSensitive: Boolean
    get() = when (this) {
        is FilePreviewSource.Local -> this.file.isSensitive
        is FilePreviewSource.Remote -> fileProperty.isSensitive
    }





fun AppFile.Companion.from(file: DraftNoteFile): AppFile {
    return when (file) {
        is DraftNoteFile.Local -> AppFile.Local(
            name = file.name,
            path = file.filePath,
            thumbnailUrl = file.thumbnailUrl,
            type = file.type,
            isSensitive = file.isSensitive ?: false,
            fileSize = file.fileSize,
            folderId = file.folderId,
            comment = file.comment,
        )
        is DraftNoteFile.Remote -> AppFile.Remote(file.fileProperty.id)
    }
}


