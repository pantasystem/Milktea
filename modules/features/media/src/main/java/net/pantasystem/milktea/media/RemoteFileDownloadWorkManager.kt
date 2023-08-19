package net.pantasystem.milktea.media

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common_android.notification.NotificationUtil
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.URL

@HiltWorker
class RemoteFileDownloadWorkManager @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted val params: WorkerParameters,
    private val notificationUtil: NotificationUtil,
    private val loggerFactory: Logger.Factory,
) : CoroutineWorker(context, params) {

    val logger by lazy {
        loggerFactory.create("RemoteFileDownloadWM")
    }

    companion object {
        const val EXTRA_DOWNLOAD_URL = "RemoteFileDownloadWorkManager.EXTRA_DOWNLOAD_URL"
        const val EXTRA_MIME_TYPE = "RemoteFileDownloadWorkManager.MIME_TYPE"
        const val EXTRA_FILE_NAME = "RemoteFileDownloadWorkManager.FILE_NAME"

        const val NOTIFICATION_CHANNEL_ID: String = "DOWNLOAD_MEDIA_NOTIFICATION"

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

        val context = applicationContext
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
                    DownloadContentType.Image -> put(MediaStore.Images.Media.DISPLAY_NAME, "$fileName.png")
                    DownloadContentType.Audio -> put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
                }
            }

            val uri = context.contentResolver.insert(mediaCollection, contentDetail)


            withContext(Dispatchers.IO) {
                URL(params.inputData.getString(EXTRA_DOWNLOAD_URL)).openConnection()
                    .getInputStream().use { inputStream ->
                        context.contentResolver.openOutputStream(uri!!)?.use { outputStream ->
                            // if image convert to bitmap
                            if (type == DownloadContentType.Image) {
                                val bitmap = BitmapFactory.decodeStream(inputStream)
                                    ?: throw IOException("Failed to decode bitmap")
                                if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
                                    outputStream.close()
                                    bitmap.recycle()
                                }
                            } else {
                                inputStream.transferToOutputStream(outputStream)
                            }
                            inputStream.transferToOutputStream(outputStream)
                        }
                    }
            }

            withContext(Dispatchers.Main) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    NotificationUtil(context)
                    val notificationManager = notificationUtil.makeNotificationManager(
                        id = NOTIFICATION_CHANNEL_ID,
                        description = "Download Result Notification",
                        name = "Download Notification"
                    )
                    showDownloadSuccessNotification(context, notificationManager, uri!!)

                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.success_download_file_message),
                        Toast.LENGTH_LONG
                    ).show()
                }

            }
            Result.success()
        } catch (e: Exception) {
            Log.e("RemoteFileDownloadWM", "download error", e)
            logger.error("download error", e)
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

    private fun showDownloadSuccessNotification(context: Context, notificationManager: NotificationManager, uri: Uri) {
        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(context.getString(R.string.success_download_file_message))
        builder.priority = NotificationCompat.PRIORITY_DEFAULT

        val pendingIntentBuilder = TaskStackBuilder.create(context)
            .addNextIntent(Intent(Intent.ACTION_VIEW).apply {
                this.data = uri
            })
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntentBuilder.getPendingIntent(0, PendingIntent.FLAG_MUTABLE)
        } else {
            pendingIntentBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        builder.setContentIntent(pendingIntent)
        with(notificationManager) {
            notify(uri.path.hashCode(), builder.build())
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