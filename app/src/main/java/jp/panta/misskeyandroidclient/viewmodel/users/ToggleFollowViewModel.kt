package jp.panta.misskeyandroidclient.viewmodel.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.api.users.RequestUser
import jp.panta.misskeyandroidclient.api.users.toUser
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.model.users.UserNotFoundException
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Suppress("BlockingMethodInNonBlockingContext")
class ToggleFollowViewModel(val miCore: MiCore) : ViewModel(){


    @Suppress("UNCHECKED_CAST")
    class Factory(val miCore: MiCore) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ToggleFollowViewModel(miCore) as T
        }
    }

    fun toggleFollow(userId: User.Id){
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

    private suspend fun getUser(userId: User.Id): User.Detail? {
        return runCatching {
            miCore.getUserRepository().find(userId, true) as User.Detail
        }.getOrNull()


    }
}