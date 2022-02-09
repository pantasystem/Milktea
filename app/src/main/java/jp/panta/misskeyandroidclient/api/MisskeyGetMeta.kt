package jp.panta.misskeyandroidclient.api

import com.google.gson.JsonSyntaxException
import jp.panta.misskeyandroidclient.GsonFactory
import jp.panta.misskeyandroidclient.model.instance.Meta
import jp.panta.misskeyandroidclient.model.instance.RequestMeta
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

object MisskeyGetMeta {

    val gson = GsonFactory.create()
    fun getMeta(baseUrl: String): Call<Meta>{
        val client = OkHttpClient()
        val requestBody = gson.toJson(RequestMeta()).toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder().url("$baseUrl/api/meta").post(requestBody).build()
        val call = client.newCall(request)
        return MetaCall(request, call)
    }

    private class MetaCall(private val request: Request, private val call: okhttp3.Call) : Call<Meta>{


        override fun request(): Request {
            return request
        }
        override fun enqueue(callback: Callback<Meta>) {
            call.enqueue(object : okhttp3.Callback{
                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    try{
                        val meta = response.body?.let{
                            gson.fromJson(it.string(), Meta::class.java)
                        }
                        callback.onResponse(this@MetaCall, Response.success(meta))
                    }catch(e: JsonSyntaxException){
                        callback.onFailure(this@MetaCall, e)
                    }


                }

                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    callback.onFailure(this@MetaCall, e)
                }
            })
        }

        override fun execute(): Response<Meta> {
            val res =  call.execute().body?.string()?.let{
                gson.fromJson(it, Meta::class.java)
            }
            return Response.success(res)
        }

        override fun isCanceled(): Boolean {
            return call.isCanceled()
        }

        override fun isExecuted(): Boolean {
            return call.isExecuted()
        }
        override fun cancel() {
            return call.cancel()
        }

        override fun clone(): Call<Meta> {
            return MetaCall(request, call)
        }

        override fun timeout(): Timeout {
            return call.timeout()
        }
    }
}