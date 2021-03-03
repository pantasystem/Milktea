package jp.panta.misskeyandroidclient.viewmodel.account

import android.util.Log
import androidx.lifecycle.*
import jp.panta.misskeyandroidclient.model.I
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.api.users.UserDTO
import jp.panta.misskeyandroidclient.api.users.toUser
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.streaming.ChannelBody
import jp.panta.misskeyandroidclient.streaming.channel.ChannelAPI
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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

    val accounts = MediatorLiveData<List<AccountViewData>>().apply{
        miCore.getAccounts().onEach { accounts ->
            value = accounts.map{ ac ->
                val avd = AccountViewData(ac)
                ac.getI(miCore.getEncryption()).let{ i ->
                    miCore.getMisskeyAPI(ac).i(
                        I(i)
                    ).enqueue(avd.accept)
                }
                avd
            }
        }.launchIn(viewModelScope + Dispatchers.IO)
    }

    val currentAccount = miCore.getCurrentAccount()

    val user = MediatorLiveData<UserDTO>()

    val switchAccount = EventBus<Int>()


    val showFollowers = EventBus<Unit>()
    val showFollowings = EventBus<Unit>()

    val showProfile = EventBus<Account>()

    val switchTargetConnectionInstanceEvent = EventBus<Unit>()

    init{
        miCore.getCurrentAccount().onEach {
            runCatching {
                it?.getI(miCore.getEncryption())?.let{ i ->
                    miCore.getMisskeyAPI(it).i(I(i)).enqueue(object : Callback<UserDTO>{
                        override fun onResponse(call: Call<UserDTO>, response: Response<UserDTO>) {
                            response.body()?.let{ u ->
                                user.postValue(u)
                            }
                        }

                        override fun onFailure(call: Call<UserDTO>, t: Throwable) {
                            user.postValue(null)
                            Log.d(TAG, "user load error", t)
                        }
                    })
                }
            }
        }.launchIn(viewModelScope + Dispatchers.IO)

        miCore.getCurrentAccount().filterNotNull().flatMapMerge { ac->
            miCore.getChannelAPI(ac).connect(ChannelAPI.Type.MAIN)
        }.map {
            it as? ChannelBody.Main.MeUpdated
        }.filterNotNull().onEach {
            user.postValue(it.body)
        }.launchIn(viewModelScope + Dispatchers.IO)
    }

    fun setSwitchTargetConnectionInstance(account: Account){
        switchTargetConnectionInstanceEvent.event = Unit
        miCore.setCurrentAccount(account)
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

    fun showProfile(account: Account?){
        account?: return
        showProfile.event = account
    }

    fun signOut(accountViewData: AccountViewData){
        viewModelScope.launch(Dispatchers.IO){
            miCore.logoutAccount(accountViewData.account)
            switchTargetConnectionInstanceEvent.event = Unit
        }
    }


}