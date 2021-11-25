package jp.panta.misskeyandroidclient.api.drive

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import com.google.gson.Gson
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.drive.FileUploadFailedException
import jp.panta.misskeyandroidclient.model.drive.FileUploader
import jp.panta.misskeyandroidclient.model.file.File
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okio.BufferedSink
import okio.source
import java.net.URL
import java.util.concurrent.TimeUnit


@Suppress("BlockingMethodInNonBlockingContext")
class OkHttpDriveFileUploader(
    val context: Context,
    val account: Account,
    val gson: Gson,
    val encryption: Encryption
) : FileUploader {
    override suspend fun upload(file: File, isForce: Boolean): FilePropertyDTO {
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
            if( isSensitive != null ) requestBodyBuilder.addFormDataPart("isSensitive", isSensitive.toString())

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
                throw FileUploadFailedException(file, null, code)
            }
        }catch(e: Exception){
            Log.w("OkHttpConnection", "post file error", e)
            throw FileUploadFailedException(file, e, null)
        }
    }


    private fun createRequestBody(uri: Uri): RequestBody{
        return object : RequestBody(){
            override fun contentType(): MediaType? {
                val map = MimeTypeMap.getSingleton()
                return map.getExtensionFromMimeType(context.contentResolver.getType(uri))
                    ?.toMediaType()
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