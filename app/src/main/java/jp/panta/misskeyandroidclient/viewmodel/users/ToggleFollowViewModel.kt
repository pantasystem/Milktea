package jp.panta.misskeyandroidclient.viewmodel.users

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.api.users.RequestUser
import jp.panta.misskeyandroidclient.api.users.UserDTO
import jp.panta.misskeyandroidclient.api.users.toUser
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.model.users.UserNotFoundException
import jp.panta.misskeyandroidclient.view.SafeUnbox
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Suppress("BlockingMethodInNonBlockingContext")
class ToggleFollowViewModel(val miCore: MiCore) : ViewModel(){
    private val accountRepository = miCore.getAccountRepository()


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
            val account = miCore.getAccountRepository().get(userId.accountId)
            val api = if(user.isFollowing) miCore.getMisskeyAPI(account)::unFollowUser else miCore.getMisskeyAPI(account)::followUser
            runCatching {
                api.invoke(RequestUser(i = account.getI(miCore.getEncryption()), userId = userId.id)).execute()
            }.getOrNull()?.body()?.let{
                miCore.getUserRepository().add(it.toUser(account, true))
            }


        }

    }

    private suspend fun getUser(userId: User.Id): User.Detail? {
        return runCatching {
            miCore.getUserRepository().get(userId) as? User.Detail
                ?: throw UserNotFoundException(userId)
        }.getOrElse {
            val account = accountRepository.get(userId.accountId)
            val i = account.getI(miCore.getEncryption())
            val misskeyAPI = miCore.getMisskeyAPI(account)
            val user = misskeyAPI.showUser(
                RequestUser(
                    i = i,
                    detail = true,
                    userId = userId.id
                )
            ).execute().body()
                ?: return@getOrElse null
            user.toUser(account).also {
                miCore.getUserRepository().add(it)
            }.let{
                it as? User.Detail
            }
        }


    }
}