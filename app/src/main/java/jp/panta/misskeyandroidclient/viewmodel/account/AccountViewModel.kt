package jp.panta.misskeyandroidclient.viewmodel.account

import androidx.lifecycle.*
import jp.panta.misskeyandroidclient.model.I
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.api.users.toUser
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.streaming.ChannelBody
import jp.panta.misskeyandroidclient.streaming.channel.ChannelAPI
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.lang.IllegalArgumentException

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
                AccountViewData(ac, miCore, viewModelScope, Dispatchers.IO)
            }
        }.launchIn(viewModelScope + Dispatchers.IO)
    }

    val currentAccount = miCore.getCurrentAccount()

    val user = MediatorLiveData<User.Detail>()

    val switchAccount = EventBus<Int>()


    val showFollowers = EventBus<User.Id>()
    val showFollowings = EventBus<User.Id>()

    val showProfile = EventBus<Account>()

    val switchTargetConnectionInstanceEvent = EventBus<Unit>()

    init{
        miCore.getCurrentAccount().filterNotNull().map { ac ->
            val res = runCatching {
                ac.getI(miCore.getEncryption()).let{ i ->
                    miCore.getMisskeyAPI(ac).i(I(i)).execute()
                }
            }.getOrNull()
            res?.body()?.let{
                ac to it
            }
        }.filterNotNull().map { pair ->
            val user = pair.second.toUser(pair.first, true)
            miCore.getUserDataSource().add(user)
            user
        }.onEach {

        }

        miCore.getCurrentAccount().filterNotNull().flatMapLatest { ac->
            miCore.getChannelAPI(ac).connect(ChannelAPI.Type.MAIN).map {
                ac to it
            }
        }.map { pair ->
            (pair.second as? ChannelBody.Main.MeUpdated)?.let{ meUpdated ->
                pair.first to meUpdated
            }
        }.filterNotNull().onEach {
            val user = it.second.body.toUser(it.first, true)
            miCore.getUserDataSource().add(user)
            this.user.postValue(user as User.Detail)
        }.launchIn(viewModelScope + Dispatchers.IO)
    }

    fun setSwitchTargetConnectionInstance(account: Account){
        switchTargetConnectionInstanceEvent.event = Unit
        miCore.setCurrentAccount(account)
    }

    fun showSwitchDialog(){
        switchAccount.event = switchAccount.event?: 0 + 1
    }

    fun showFollowers(userId: User.Id){
        showFollowers.event = userId
    }

    fun showFollowings(userId: User.Id){
        showFollowings.event = userId
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