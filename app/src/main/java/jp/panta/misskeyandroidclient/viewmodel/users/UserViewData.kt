package jp.panta.misskeyandroidclient.viewmodel.users

import android.util.Log
import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.api.users.RequestUser
import jp.panta.misskeyandroidclient.api.users.UserDTO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class UserViewData{

    val userId: String
    val user: MutableLiveData<UserDTO?> = object : MutableLiveData<UserDTO?>(){
        override fun onActive() {
            super.onActive()
            val userField = value
            if(userField == null){
                api?.invoke()?.enqueue(accept)
            }
        }

    }


    constructor(userId: String){
        this.userId = userId
    }

    constructor(user: UserDTO): this(user.id){
        this.user.postValue(user)
    }

    val accept = object : Callback<UserDTO>{
        override fun onResponse(call: Call<UserDTO>, response: Response<UserDTO>) {
            val user = response.body()
            if(user != null){
                this@UserViewData.user.postValue(user)
            }
        }

        override fun onFailure(call: Call<UserDTO>, t: Throwable) {
            Log.e("UserViewData", "user load error", t)
        }
    }


    var api: (()-> Call<UserDTO>)? = null

    fun setApi(i: String, misskeyAPI: MisskeyAPI){
        api = {
            misskeyAPI.showUser(RequestUser(i = i, userId = userId))
        }
    }
}