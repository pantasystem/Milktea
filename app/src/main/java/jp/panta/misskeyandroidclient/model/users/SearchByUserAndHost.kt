package jp.panta.misskeyandroidclient.model.users

import com.google.gson.reflect.TypeToken
import jp.panta.misskeyandroidclient.GsonFactory
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.api.MisskeyGetMeta
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.meta.RequestMeta
import jp.panta.misskeyandroidclient.model.v12.MisskeyAPIV12
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class SearchByUserAndHost(val misskeyAPI: MisskeyAPI){

    companion object{
        private val gson = GsonFactory.create()

    }

    fun search(reqUser: RequestUser): Call<List<User>>{


        val requestUser = reqUser.copy(
            host = reqUser.host?: ""
        )
        return if(misskeyAPI is MisskeyAPIV12){
            misskeyAPI.searchByUserNameAndHost(requestUser)
        }else{

            SearchUserCall(misskeyAPI.searchUser(requestUser.copy(host = "", userName = null, query = requestUser.userName)), requestUser)
        }
    }

    private class SearchUserCall(val call: Call<List<User>>, val reqUser: RequestUser) : Call<List<User>>{
        override fun cancel() {
            call.cancel()
        }

        override fun clone(): Call<List<User>> {
            return call.clone()
        }

        override fun enqueue(callback: Callback<List<User>>) {
            if(reqUser.host.isNullOrBlank()){
                call.enqueue(callback)
                return
            }

            call.enqueue(object : Callback<List<User>>{
                override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                    val list = response.body()?.filter{
                        it.host?.contains(reqUser.host) == true
                    }
                    Response.success(list)
                }

                override fun onFailure(call: Call<List<User>>, t: Throwable) {
                    callback.onFailure(call, t)
                }
            })
        }

        override fun execute(): Response<List<User>> {
            if(reqUser.host.isNullOrBlank()){
                return call.execute()
            }
            val list: List<User>? = call.execute().body()?.filter{
                it.host?.contains(reqUser.host) == true
            }
            return Response.success(list)
        }

        override fun isCanceled(): Boolean {
            return call.isCanceled
        }

        override fun isExecuted(): Boolean {
            return call.isExecuted
        }

        override fun request(): Request {
            return call.request()
        }
    }
}