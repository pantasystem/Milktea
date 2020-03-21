package jp.panta.misskeyandroidclient.viewmodel.list

import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.model.users.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ListUserViewData (val userId: String){
    val user = MutableLiveData<User>()

    val accept = object : Callback<User>{
        override fun onResponse(call: Call<User>, response: Response<User>) {
            user.postValue(response.body())
        }

        override fun onFailure(call: Call<User>, t: Throwable) {

        }
    }
}