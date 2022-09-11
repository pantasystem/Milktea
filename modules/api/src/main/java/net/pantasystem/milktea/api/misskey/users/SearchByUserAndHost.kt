package net.pantasystem.milktea.api.misskey.users

import net.pantasystem.milktea.api.misskey.MisskeyAPI
import net.pantasystem.milktea.api.misskey.v12.MisskeyAPIV12
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