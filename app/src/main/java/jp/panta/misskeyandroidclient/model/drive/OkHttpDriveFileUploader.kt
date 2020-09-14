package jp.panta.misskeyandroidclient.model.drive

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import com.google.gson.Gson
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.core.EncryptedConnectionInformation
import jp.panta.misskeyandroidclient.model.file.File
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import okio.BufferedSink
import okio.Okio
import java.net.CookieManager
import java.net.CookiePolicy
import java.net.URL


class OkHttpDriveFileUploader(
    val context: Context,
    val account: Account,
    val gson: Gson,
    val encryption: Encryption
) : FileUploader{
    override fun upload(file: File, isForce: Boolean): FileProperty? {
        Log.d("FileUploader", "アップロードしようとしている情報:$file")
        return try{

            val client = OkHttpClient()

            val requestBodyBuilder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("i", account.getI(encryption)!!)
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
            val code = response.code()
            if(code in 200 until 300){
                gson.fromJson(response.body()?.string(), FileProperty::class.java)
            }else{
                Log.d("OkHttpConnection", "code: $code, error${response.body()?.string()}")
                null
            }
        }catch(e: Exception){
            Log.w("OkHttpConnection", "post file error", e)
            null
        }
    }

    private fun getFileName(uri: Uri) : String{
        return when(uri.scheme){
            "content" ->{
                context.contentResolver
                    .query(uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)?.use{
                        if(it.moveToFirst()){
                            it.getString(it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME))
                        }else{
                            null
                        }
                    }?: throw IllegalArgumentException("ファイル名の取得に失敗しました")
            }
            "file" ->{
                java.io.File(uri.path!!).name
            }
            else -> throw IllegalArgumentException("scheme不明")
        }
    }


    private fun createRequestBody(uri: Uri): RequestBody{
        return object : RequestBody(){
            override fun contentType(): MediaType? {
                val map = MimeTypeMap.getSingleton()
                val mime = map.getExtensionFromMimeType(context.contentResolver.getType(uri))
                return if(mime != null){
                    MediaType.parse(mime)
                }else{
                    null
                }
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
                if(inputStream != null){
                    Okio.source(inputStream)
                        .use{
                            sink.writeAll(it)
                        }
                }

            }
        }
    }
}