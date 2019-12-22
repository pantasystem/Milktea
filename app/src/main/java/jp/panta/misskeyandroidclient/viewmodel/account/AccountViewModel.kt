package jp.panta.misskeyandroidclient.viewmodel.account

import androidx.lifecycle.ViewModel
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.util.eventbus.EventBus

class AccountViewModel : ViewModel(){

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

}