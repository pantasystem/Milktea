package jp.panta.misskeyandroidclient.viewmodel.users

import android.util.Log
import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.users.RequestUser
import jp.panta.misskeyandroidclient.model.users.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserViewData{

    val userId: String
    val user: MutableLiveData<User?> = object : MutableLiveData<User?>(){
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

    constructor(user: User): this(user.id){
        this.user.postValue(user)
    }

    val accept = object : Callback<User>{
        override fun onResponse(call: Call<User>, response: Response<User>) {
            val user = response.body()
            if(user != null){
                this@UserViewData.user.postValue(user)
            }
        }

        override fun onFailure(call: Call<User>, t: Throwable) {
            Log.e("UserViewData", "user load error", t)
        }
    }


    var api: (()-> Call<User>)? = null

    fun setApi(i: String, misskeyAPI: MisskeyAPI){
        api = {
            misskeyAPI.showUser(RequestUser(i = i, userId = userId))
        }
    }
}