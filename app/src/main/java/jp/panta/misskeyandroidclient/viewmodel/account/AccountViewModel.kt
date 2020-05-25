package jp.panta.misskeyandroidclient.viewmodel.account

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.model.I
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import java.lang.IllegalArgumentException
import retrofit2.Callback
import retrofit2.Response

@Suppress("UNCHECKED_CAST")
class AccountViewModel(
    val miCore: MiCore
) : ViewModel(){

    class Factory(val miCore: MiCore) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if(modelClass == AccountViewModel::class.java){
                return AccountViewModel(miCore) as T
            }
            throw IllegalArgumentException("use AccountViewModel::class.java")
        }
    }

    companion object{
        const val TAG = "AccountViewModel"
    }

    val user = MutableLiveData<User>()

    val switchAccount = EventBus<Int>()


    val showFollowers = EventBus<Unit>()
    val showFollowings = EventBus<Unit>()

    val showProfile = EventBus<User>()

    val switchTargetConnectionInstanceEvent = EventBus<Unit>()

    init{
        miCore.currentAccount.observeForever {
            val ci = it.getCurrentConnectionInformation()

            val nullableI = ci?.getI(miCore.getEncryption())
            nullableI?.let { i ->
                miCore.getMisskeyAPI(ci).i(I(i)).enqueue(object : Callback<User>{
                    override fun onResponse(call: Call<User>, response: Response<User>) {
                        response.body()?.let{ u ->
                            user.postValue(u)
                        }
                    }

                    override fun onFailure(call: Call<User>, t: Throwable) {
                        user.postValue(null)
                        Log.d(TAG, "user load error", t)
                    }
                })
            }

        }

    }

    fun setSwitchTargetConnectionInstance(account: AccountRelation){
        switchTargetConnectionInstanceEvent.event = Unit
        miCore.switchAccount(account.account)
    }

    fun showSwitchDialog(){
        switchAccount.event = switchAccount.event?: 0 + 1
    }

    fun showFollowers(){
        showFollowers.event = Unit
    }

    fun showFollowings(){
        showFollowings.event = Unit
    }

    fun showProfile(user: User){
        showProfile.event = user
    }

    fun signOut(accountViewData: AccountViewData){
        viewModelScope.launch(Dispatchers.IO){
            miCore.logoutAccount(accountViewData.accountRelation.account)
            switchTargetConnectionInstanceEvent.event = Unit
        }
    }


}