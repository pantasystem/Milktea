package net.pantasystem.milktea.data.infrastructure.drive

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source


internal class UriRequestBody(
    val uri: Uri,
    val context: Context,
) : RequestBody() {
    override fun contentType(): MediaType? {
        val type = context.contentResolver.getType(uri)
        return type?.toMediaType()
    }


    override fun writeTo(sink: BufferedSink) {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->

            val type = context.contentResolver.getType(uri)
            if (type?.startsWith("image") == true
                && (type.split("/").getOrNull(1)?.let {
                    it == "jpeg" || it == "jpg"
                } == true)
            ) {

                val bitmap =
                    rotateImageIfRequired(BitmapFactory.decodeStream(inputStream), uri)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, sink.outputStream())
                bitmap.recycle()

            } else {
                sink.writeAll(inputStream.source())
            }
        }
    }

    /**
     * 与えられたBitmapを元ファイルのExif情報を元に回転させる処理
     */
    private fun rotateImageIfRequired(bitmap: Bitmap, uri: Uri): Bitmap {
        context.contentResolver.openFileDescriptor(uri, "r").use {
            val fileDescriptor = it!!.fileDescriptor
            val exif = ExifInterface(fileDescriptor)

            return when (exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270)
                else -> bitmap
            }
        }


    }

    /**
     * 回転状態に基づきBitmapを回転させる
     */
    private fun rotateImage(bitmap: Bitmap, degree: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        bitmap.recycle()
        return rotated
    }
}