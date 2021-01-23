package jp.panta.misskeyandroidclient.viewmodel.users

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.api.users.RequestUser
import jp.panta.misskeyandroidclient.api.users.User
import jp.panta.misskeyandroidclient.view.SafeUnbox
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ToggleFollowViewModel(val miCore: MiCore) : ViewModel(){

    @Suppress("UNCHECKED_CAST")
    class Factory(val miCore: MiCore) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ToggleFollowViewModel(miCore) as T
        }
    }

    fun toggleFollow(user: User?){
        user?: return
        val ac = miCore.getCurrentAccount().value
        val i = ac?.getI(miCore.getEncryption())
            ?: return
        val misskeyAPI = miCore.getMisskeyAPI(ac)
        val api = if(SafeUnbox.unbox(user.isFollowing)){
            misskeyAPI::unFollowUser
        }else{
            misskeyAPI::followUser
        }


        api.invoke(RequestUser(i = i, userId = user.id)).enqueue(object : Callback<User>{
            override fun onFailure(call: Call<User>, t: Throwable) {
                Log.e("ToggleFollowViewModel", "toggle follow error", t)
            }

            override fun onResponse(call: Call<User>, response: Response<User>) {
                if(response.code() in 200 until 300){
                    Log.d("ToggleFollowViewModel", "success follow user:${response.body()}")
                }else{
                    Log.e("ToggleFollowViewModel", "toggle follow error: ${response.errorBody()}, code:${response.code()}")
                }
            }
        })
    }
}