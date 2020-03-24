package jp.panta.misskeyandroidclient.viewmodel.list

import android.util.Log
import androidx.lifecycle.MutableLiveData
import jp.panta.misskeyandroidclient.model.users.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ListUserViewData (val userId: String){
    companion object{
        private const val TAG = "ListUserViewData"
    }
    val user = MutableLiveData<User>()

    val accept = object : Callback<User>{
        override fun onResponse(call: Call<User>, response: Response<User>) {
            val responseUser = response.body()
            Log.d(TAG, "load user success user:$responseUser")
            user.postValue(responseUser)
        }

        override fun onFailure(call: Call<User>, t: Throwable) {
            Log.e(TAG, "show user error", t)
        }
    }
}