package jp.panta.misskeyandroidclient.api.users

import jp.panta.misskeyandroidclient.GsonFactory
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.api.v12.MisskeyAPIV12
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchByUserAndHost(val misskeyAPI: MisskeyAPI){


    suspend fun search(reqUser: RequestUser): Response<List<UserDTO>>{


        val requestUser = reqUser.copy(
            host = reqUser.host?: ""
        )
        return if(misskeyAPI is MisskeyAPIV12){
            misskeyAPI.searchByUserNameAndHost(requestUser)
        }else{

            misskeyAPI.searchUser(requestUser.copy(host = "", userName = null, query = requestUser.userName))
        }
    }


}