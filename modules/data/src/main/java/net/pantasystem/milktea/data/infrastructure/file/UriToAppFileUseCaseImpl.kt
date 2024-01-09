package net.pantasystem.milktea.data.infrastructure.file

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import net.pantasystem.milktea.model.file.AppFile
import net.pantasystem.milktea.model.file.UriToAppFileUseCase
import javax.inject.Inject

class UriToAppFileUseCaseImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UriToAppFileUseCase {
    override operator fun invoke(uri: Uri): AppFile.Local {
        return uri.toAppFile(context)
    }

}


internal fun Uri.toAppFile(context: Context): AppFile.Local {
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