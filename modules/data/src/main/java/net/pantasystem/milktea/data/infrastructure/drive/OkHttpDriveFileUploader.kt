package net.pantasystem.milktea.data.infrastructure.drive

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.pantasystem.milktea.api.misskey.OkHttpClientProvider
import net.pantasystem.milktea.api.misskey.drive.FilePropertyDTO
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.file.AppFile
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okio.BufferedSink
import okio.source
import java.io.InputStream
import java.net.URL
import java.util.concurrent.TimeUnit

object OkHttpDriveFileUploaderConstants {
    const val i = "i"
    const val force = "force"
    const val file = "file"
    const val folderId = "folderId"
    const val isSensitive = "isSensitive"
}

@Suppress("BlockingMethodInNonBlockingContext")
class OkHttpDriveFileUploader(
    val context: Context,
    val account: Account,
    val json: Json,
    private val okHttpClientProvider: OkHttpClientProvider,
) : FileUploader {
    override suspend fun upload(file: UploadSource, isForce: Boolean): FilePropertyDTO {
        return when (file) {
            is UploadSource.LocalFile -> upload(file.file, isForce)
            is UploadSource.OtherAccountFile -> transferUpload(file.fileProperty, isForce)
        }
    }

    private fun transferUpload(fileProperty: FileProperty, isForce: Boolean): FilePropertyDTO {
        return try {
            val client = getOkHttpClient()
            val res = client.newCall(Request.Builder().url(fileProperty.url).build())

            val requestBodyBuilder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(OkHttpDriveFileUploaderConstants.i, account.token)
                .addFormDataPart(OkHttpDriveFileUploaderConstants.force, isForce.toString())
                //.addFormDataPart("file", uploadFile.file.name, RequestBody.create(MediaType.parse(mime), uploadFile.file))
                .addFormDataPart(
                    OkHttpDriveFileUploaderConstants.file,
                    fileProperty.name,
                    createRequestBody(fileProperty.type, res.execute().body!!.byteStream())
                )

            if (fileProperty.folderId != null) {
                requestBodyBuilder.addFormDataPart(
                    OkHttpDriveFileUploaderConstants.folderId,
                    fileProperty.folderId!!
                )
            }

            val requestBody = requestBodyBuilder.build()

            val request =
                Request.Builder().url(URL("${account.normalizedInstanceDomain}/api/drive/files/create"))
                    .post(requestBody).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                json.decodeFromString<FilePropertyDTO>(response.body!!.string())
            } else {
                throw FileUploadFailedException(
                    AppFile.Remote(fileProperty.id),
                    null,
                    response.code,
                    response.body?.string()
                )
            }
        } catch (e: Throwable) {
            throw FileUploadFailedException(
                AppFile.Remote(fileProperty.id),
                e,
                null,
                null,
            )
        }


    }

    private fun upload(file: AppFile.Local, isForce: Boolean): FilePropertyDTO {
        Log.d("FileUploader", "アップロードしようとしている情報:$file")
        return try {


            val client = getOkHttpClient()
            val requestBodyBuilder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(OkHttpDriveFileUploaderConstants.i, account.token)
                .addFormDataPart(OkHttpDriveFileUploaderConstants.force, isForce.toString())
                //.addFormDataPart("file", uploadFile.file.name, RequestBody.create(MediaType.parse(mime), uploadFile.file))
                .addFormDataPart(
                    OkHttpDriveFileUploaderConstants.file,
                    file.name,
                    createRequestBody(Uri.parse(file.path))
                )

            val isSensitive = file.isSensitive
            requestBodyBuilder.addFormDataPart(
                OkHttpDriveFileUploaderConstants.isSensitive,
                isSensitive.toString()
            )

            val folderId = file.folderId
            if (folderId != null) requestBodyBuilder.addFormDataPart(
                OkHttpDriveFileUploaderConstants.folderId,
                folderId
            )

            val requestBody = requestBodyBuilder.build()

            val request =
                Request.Builder().url(URL("${account.normalizedInstanceDomain}/api/drive/files/create"))
                    .post(requestBody).build()
            val response = client.newCall(request).execute()
            val code = response.code
            if (code in 200 until 300) {
                json.decodeFromString<FilePropertyDTO>(response.body!!.string())
            } else {
                val resBody = response.body?.string()
                Log.d("OkHttpConnection", "code: $code, error$resBody")
                throw FileUploadFailedException(
                    file,
                    null,
                    code,
                    resBody,
                )
            }
        } catch (e: Exception) {
            Log.w("OkHttpConnection", "post file error", e)
            throw FileUploadFailedException(file, e, null, null)
        }
    }


    private fun createRequestBody(type: String, inputStream: InputStream): RequestBody {
        return InputStreamRequestBody(type = type, inputStream = inputStream)
    }

    private fun createRequestBody(uri: Uri): RequestBody {
        return UriRequestBody(uri, context)
    }

    private fun getOkHttpClient(): OkHttpClient {
        return okHttpClientProvider.get().newBuilder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(114514, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()

    }



}

private class UriRequestBody(
    val uri: Uri,
    val context: Context,
) : RequestBody() {
    override fun contentType(): MediaType? {
        val type = context.contentResolver.getType(uri)
        return type?.toMediaType()
    }

    override fun contentLength(): Long {
        return context.contentResolver.query(
            uri,
            arrayOf(MediaStore.MediaColumns.SIZE),
            null,
            null,
            null
        )?.use {
            return@use if (it.moveToFirst()) {
                it.getLong(0)
            } else {
                null
            }
        } ?: throw IllegalArgumentException("ファイルサイズの取得に失敗しました")
    }

    override fun writeTo(sink: BufferedSink) {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            sink.writeAll(inputStream.source())
        }
    }
}

private class InputStreamRequestBody(
    val type: String,
    val inputStream: InputStream,
) : RequestBody() {
    override fun contentType(): MediaType {
        return type.toMediaType()
    }

    override fun writeTo(sink: BufferedSink) {
        inputStream.source().use {
            sink.writeAll(it)
        }
    }
}