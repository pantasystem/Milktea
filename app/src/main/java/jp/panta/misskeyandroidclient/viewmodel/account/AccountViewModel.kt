package jp.panta.misskeyandroidclient.viewmodel.account

import androidx.lifecycle.ViewModel
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.util.eventbus.EventBus

class AccountViewModel : ViewModel(){

    val switchAccount = EventBus<Int>()

    val switchTargetConnectionInstance = EventBus<ConnectionInstance>()

    fun setSwitchTargetConnectionInstance(connectionInstance: ConnectionInstance){
        switchTargetConnectionInstance.event = connectionInstance
    }

    fun showSwitchDialog(){
        switchAccount.event = switchAccount.event?: 0 + 1
    }


}