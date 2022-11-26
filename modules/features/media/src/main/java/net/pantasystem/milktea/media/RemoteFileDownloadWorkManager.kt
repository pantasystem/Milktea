package net.pantasystem.milktea.media

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.URL

class RemoteFileDownloadWorkManager(
    private val context: Context,
    private val params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val EXTRA_DOWNLOAD_URL = "RemoteFileDownloadWorkManager.EXTRA_DOWNLOAD_URL"
        const val EXTRA_MIME_TYPE = "RemoteFileDownloadWorkManager.MIME_TYPE"
        const val EXTRA_FILE_NAME = "RemoteFileDownloadWorkManager.FILE_NAME"

        fun createWorkRequest(
            type: DownloadContentType,
            url: String,
            name: String
        ): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<RemoteFileDownloadWorkManager>()
                .setInputData(
                    Data.Builder()
                        .putString(EXTRA_FILE_NAME, name)
                        .putString(EXTRA_DOWNLOAD_URL, url)
                        .putInt(EXTRA_MIME_TYPE, type.ordinal)
                        .build()
                ).build()
        }
    }

    override suspend fun doWork(): Result {

        return try {
            val fileName = params.inputData.getString(EXTRA_FILE_NAME)!!
            val type = DownloadContentType.values()[params.inputData.getInt(EXTRA_MIME_TYPE, 0)]
            val mediaCollection = when (type) {
                DownloadContentType.Video -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        MediaStore.Video.Media.getContentUri(
                            MediaStore.VOLUME_EXTERNAL_PRIMARY
                        )
                    } else {
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    }
                }
                DownloadContentType.Image -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        MediaStore.Images.Media.getContentUri(
                            MediaStore.VOLUME_EXTERNAL_PRIMARY
                        )
                    } else {
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    }
                }
                DownloadContentType.Audio -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        MediaStore.Audio.Media.getContentUri(
                            MediaStore.VOLUME_EXTERNAL_PRIMARY
                        )
                    } else {
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                }
            }


            val contentDetail = ContentValues().apply {
                when (type) {
                    DownloadContentType.Video -> put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                    DownloadContentType.Image -> put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    DownloadContentType.Audio -> put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
                }
            }

            val uri = context.contentResolver.insert(mediaCollection, contentDetail)

            withContext(Dispatchers.IO) {
                URL(params.inputData.getString(EXTRA_DOWNLOAD_URL)).openConnection()
                    .getInputStream().use { inputStream ->
                        context.contentResolver.openOutputStream(uri!!).use { outputStream ->
                            inputStream.transferToOutputStream(outputStream!!)
                        }
                    }
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    context.getString(R.string.success_download_file_message),
                    Toast.LENGTH_LONG
                ).show()
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("RemoteFileDownloadWM", "download error", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    context.getString(R.string.failed_download_file_message),
                    Toast.LENGTH_LONG
                ).show()
            }
            Result.failure()
        }
    }
}

enum class DownloadContentType {
    Video, Image, Audio
}

@Throws(IOException::class)
fun InputStream.transferToOutputStream(out: OutputStream): Long {
    var transferred: Long = 0
    val buffer = ByteArray(8192)
    var read: Int
    while (this.read(buffer, 0, 8192).also { read = it } >= 0) {
        out.write(buffer, 0, read)
        transferred += read.toLong()
    }
    return transferred
}