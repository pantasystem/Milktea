package jp.panta.misskeyandroidclient.api.users

import jp.panta.misskeyandroidclient.GsonFactory
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.api.v12.MisskeyAPIV12
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchByUserAndHost(val misskeyAPI: MisskeyAPI){

    companion object{
        private val gson = GsonFactory.create()

    }

    fun search(reqUser: RequestUser): Call<List<UserDTO>>{


        val requestUser = reqUser.copy(
            host = reqUser.host?: ""
        )
        return if(misskeyAPI is MisskeyAPIV12){
            misskeyAPI.searchByUserNameAndHost(requestUser)
        }else{

            SearchUserCall(misskeyAPI.searchUser(requestUser.copy(host = "", userName = null, query = requestUser.userName)), requestUser)
        }
    }

    private class SearchUserCall(val call: Call<List<UserDTO>>, val reqUser: RequestUser) : Call<List<UserDTO>>{
        override fun cancel() {
            call.cancel()
        }

        override fun clone(): Call<List<UserDTO>> {
            return call.clone()
        }

        override fun enqueue(callback: Callback<List<UserDTO>>) {
            if(reqUser.host.isNullOrBlank()){
                call.enqueue(callback)
                return
            }

            call.enqueue(object : Callback<List<UserDTO>>{
                override fun onResponse(call: Call<List<UserDTO>>, response: Response<List<UserDTO>>) {
                    val list = response.body()?.filter{
                        it.host?.contains(reqUser.host) == true
                    }
                    Response.success(list)
                }

                override fun onFailure(call: Call<List<UserDTO>>, t: Throwable) {
                    callback.onFailure(call, t)
                }
            })
        }

        override fun execute(): Response<List<UserDTO>> {
            if(reqUser.host.isNullOrBlank()){
                return call.execute()
            }
            val list: List<UserDTO>? = call.execute().body()?.filter{
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