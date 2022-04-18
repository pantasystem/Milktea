package net.pantasystem.milktea.data.model.drive

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.google.gson.Gson
import net.pantasystem.milktea.api.misskey.drive.FilePropertyDTO
import net.pantasystem.milktea.common.Encryption
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okio.BufferedSink
import okio.source
import java.net.URL
import java.util.concurrent.TimeUnit
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.file.AppFile




@Suppress("BlockingMethodInNonBlockingContext")
class OkHttpDriveFileUploader(
    val context: Context,
    val account: Account,
    val gson: Gson,
    val encryption: Encryption
) : FileUploader {
    override suspend fun upload(file: AppFile.Local, isForce: Boolean): FilePropertyDTO {
        Log.d("FileUploader", "アップロードしようとしている情報:$file")
        return try{

            val client = OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(114514, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build()

            val requestBodyBuilder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("i", account.getI(encryption))
                .addFormDataPart("force", isForce.toString())
                //.addFormDataPart("file", uploadFile.file.name, RequestBody.create(MediaType.parse(mime), uploadFile.file))
                .addFormDataPart("file", file.name, createRequestBody(Uri.parse(file.path)))

            val isSensitive = file.isSensitive
            requestBodyBuilder.addFormDataPart("isSensitive", isSensitive.toString())

            val folderId = file.folderId
            if( folderId != null ) requestBodyBuilder.addFormDataPart("folderId", folderId)

            val requestBody = requestBodyBuilder.build()

            val request = Request.Builder().url(URL("${account.instanceDomain}/api/drive/files/create")).post(requestBody).build()
            val response = client.newCall(request).execute()
            val code = response.code
            if(code in 200 until 300){
                gson.fromJson(response.body?.string(), FilePropertyDTO::class.java)
            }else{
                Log.d("OkHttpConnection", "code: $code, error${response.body?.string()}")
                throw FileUploadFailedException(
                    file,
                    null,
                    code
                )
            }
        }catch(e: Exception){
            Log.w("OkHttpConnection", "post file error", e)
            throw FileUploadFailedException(file, e, null)
        }
    }


    private fun createRequestBody(uri: Uri): RequestBody{
        return object : RequestBody(){
            override fun contentType(): MediaType? {
                val type = context.contentResolver.getType(uri)
                return type?.toMediaType()
            }

            override fun contentLength(): Long {
                return context.contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.SIZE), null, null, null)?.use{
                    return@use if(it.moveToFirst()){
                        it.getLong(0)
                    }else{
                        null
                    }
                }?: throw IllegalArgumentException("ファイルサイズの取得に失敗しました")
            }

            override fun writeTo(sink: BufferedSink) {
                val inputStream = context.contentResolver.openInputStream(uri)
                inputStream?.source()
                    ?.use{
                        sink.writeAll(it)
                    }

            }
        }
    }
}