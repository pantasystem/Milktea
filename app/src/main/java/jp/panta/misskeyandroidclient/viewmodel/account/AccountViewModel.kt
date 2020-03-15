package jp.panta.misskeyandroidclient.viewmodel.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstanceDao
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    val switchAccount = EventBus<Int>()

    val switchTargetConnectionInstance = EventBus<ConnectionInstance>()

    val showFollowers = EventBus<Unit>()
    val showFollowings = EventBus<Unit>()

    val showProfile = EventBus<User>()

    fun setSwitchTargetConnectionInstance(connectionInstance: ConnectionInstance){
        switchTargetConnectionInstance.event = connectionInstance
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
        }
    }

}