package net.pantasystem.milktea.model.file

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
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


fun Uri.toAppFile(context: Context): AppFile.Local {
    val fileName = try {
        context.getFileName(this)
    } catch (e: Exception) {
        Log.d("FileUtils", "ファイル名の取得に失敗しました", e)
        null
    }

    val mimeType = context.contentResolver.getType(this)

    val isMedia = mimeType?.startsWith("image") ?: false || mimeType?.startsWith("video") ?: false
    val thumbnail = if (isMedia) this.toString() else null
    val fileSize = getFileSize(context)
    return AppFile.Local(
        fileName ?: "name none",
        path = this.toString(),
        type = mimeType ?: "",
        thumbnailUrl = thumbnail,
        isSensitive = false,
        folderId = null,
        fileSize = fileSize,
        comment = null,
    )
}

fun Uri.getFileSize(context: Context): Long {
    var fileSize: Long = -1
    context.contentResolver.query(this, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            fileSize = cursor.getLong(cursor.getColumnIndexOrThrow(OpenableColumns.SIZE))
        }
    }
    return fileSize
}


private fun Context.getFileName(uri: Uri): String {
    return when (uri.scheme) {
        "content" -> {
            this.contentResolver
                .query(uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)?.use {
                    if (it.moveToFirst()) {
                        val index = it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                        if (index != -1) {
                            it.getString(index)
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                } ?: throw IllegalArgumentException("ファイル名の取得に失敗しました")
        }
        "file" -> {
            java.io.File(uri.path!!).name
        }
        else -> throw IllegalArgumentException("scheme不明")
    }
}