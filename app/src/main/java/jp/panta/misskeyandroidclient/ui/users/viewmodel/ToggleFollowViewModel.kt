package jp.panta.misskeyandroidclient.ui.users.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import net.pantasystem.milktea.model.user.User
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Suppress("BlockingMethodInNonBlockingContext")
class ToggleFollowViewModel(val miCore: MiCore) : ViewModel(){


    @Suppress("UNCHECKED_CAST")
    class Factory(val miCore: MiCore) : ViewModelProvider.Factory{
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ToggleFollowViewModel(miCore) as T
        }
    }

    fun toggleFollow(userId: net.pantasystem.milktea.model.user.User.Id){
        viewModelScope.launch(Dispatchers.IO) {

            val user = runCatching {
                getUser(userId)
            }.getOrNull() ?: return@launch
            runCatching {
                if(user.isFollowing) {
                    miCore.getUserRepository().unfollow(userId)
                }else{
                    miCore.getUserRepository().follow(userId)
                }
            }


        }

    }

    private suspend fun getUser(userId: net.pantasystem.milktea.model.user.User.Id): net.pantasystem.milktea.model.user.User.Detail? {
        return runCatching {
            miCore.getUserRepository().find(userId, true) as net.pantasystem.milktea.model.user.User.Detail
        }.getOrNull()


    }
}