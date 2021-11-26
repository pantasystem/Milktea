package jp.panta.misskeyandroidclient.viewmodel.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.api.users.UserDTO
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import java.lang.IllegalArgumentException

class UserActionViewModel : ViewModel(){

    @Suppress("UNCHECKED_CAST")
    class Factory : ViewModelProvider.Factory{
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if(modelClass == UserActionViewModel::class.java){
                return UserActionViewModel() as T
            }
            throw IllegalArgumentException("use UserActionViewModel::class.java")
        }
    }

    val targetUser = EventBus<UserDTO>()

    fun setTargetToUser(user: UserDTO){
        targetUser.event = user
    }

}