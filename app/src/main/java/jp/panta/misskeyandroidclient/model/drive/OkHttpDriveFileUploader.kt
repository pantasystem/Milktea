package jp.panta.misskeyandroidclient.model.drive

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import okhttp3.*
import java.io.File
import java.net.URL

class OkHttpDriveFileUploader(
    val connectionInstance: ConnectionInstance,
    val gson: Gson
) : FileUploader{
    override fun upload(uploadFile: UploadFile): FileProperty? {
        return try{
            val mime = "image/jpg"
            val client = OkHttpClient()
            val requestBodyBuilder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("i", connectionInstance.getI()!!)
                .addFormDataPart("force", uploadFile.force.toString())
                .addFormDataPart("file", uploadFile.file.name, RequestBody.create(MediaType.parse(mime), uploadFile.file))

            val isSensitive = uploadFile.isSensitive
            if( isSensitive != null ) requestBodyBuilder.addFormDataPart("isSensitive", isSensitive.toString())

            val folderId = uploadFile.folderId
            if( folderId != null ) requestBodyBuilder.addFormDataPart("folderId", folderId)

            val requestBody = requestBodyBuilder.build()

            val request = Request.Builder().url(URL("${connectionInstance.instanceBaseUrl}/")).post(requestBody).build()
            val response = client.newCall(request).execute()
            val code = response.code()
            if(code < 300){
                gson.fromJson(response.body()?.string(), FileProperty::class.java)
            }else{
                Log.d("OkHttpConnection", "error${response.body()?.string()}")
                null
            }
        }catch(e: Exception){
            Log.w("OkHttpConnection", "post file error", e)
            null
        }
    }
}