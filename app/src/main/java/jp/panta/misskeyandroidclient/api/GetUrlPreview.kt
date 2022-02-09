package jp.panta.misskeyandroidclient.api

import com.google.gson.JsonSyntaxException
import jp.panta.misskeyandroidclient.GsonFactory
import jp.panta.misskeyandroidclient.model.url.UrlPreview
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

object GetUrlPreview {

    val gson = GsonFactory.create()
    private val client = OkHttpClient()

    fun getUrlPreview(instanceBaseUrl: String, targetUrl: String): Call<UrlPreview>{
        val params = HashMap<String, String>()
        params["url"] = targetUrl
        val request = Request.Builder().url("$instanceBaseUrl/url?url=$targetUrl").get().build()

        val call = client.newCall(request)
        return PreviewUrlCall(request, call)
    }



    private class PreviewUrlCall(private val request: Request, private val call: okhttp3.Call): Call<UrlPreview>{
        @Throws(IOException::class, JsonSyntaxException::class)
        override fun execute(): Response<UrlPreview> {
            val res = call.execute()
            val body = res.body
            val data = body?.string()?.let{
                try{
                    gson.fromJson(it, UrlPreview::class.java)
                }catch(e: JsonSyntaxException){
                    throw IllegalArgumentException("jsonと型が合わないよ！！")
                }
            }

            return if(data == null){
                if(body == null){
                    throw IllegalArgumentException("取得に失敗しました")
                }else{
                    Response.error(body, res)
                }
            }else{
                Response.success(data)
            }
        }

        override fun enqueue(callback: Callback<UrlPreview>) {
            call.enqueue(object : okhttp3.Callback{
                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    try{
                        val data =  gson.fromJson(response.body?.string(), UrlPreview::class.java)
                        callback.onResponse(this@PreviewUrlCall,Response.success(data))
                    }catch (e: JsonSyntaxException){
                        callback.onFailure(this@PreviewUrlCall, e)
                    }catch(e: Exception){
                        callback.onFailure(this@PreviewUrlCall, e)
                    }

                }

                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    callback.onFailure(this@PreviewUrlCall, e)
                }
            })
        }

        override fun cancel() {
            call.cancel()
        }

        override fun request(): Request = request

        override fun isExecuted(): Boolean = call.isExecuted()

        override fun isCanceled(): Boolean = call.isCanceled()

        override fun clone(): Call<UrlPreview> {
            return PreviewUrlCall(request, call.clone())
        }

        override fun timeout(): Timeout {
            return call.timeout()
        }
    }
}