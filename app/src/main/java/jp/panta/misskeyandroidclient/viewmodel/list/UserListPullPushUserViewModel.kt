package jp.panta.misskeyandroidclient.viewmodel.list

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.api.list.ListUserOperation
import jp.panta.misskeyandroidclient.api.throwIfHasError
import jp.panta.misskeyandroidclient.model.list.UserList
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserListPullPushUserViewModel(val miCore: MiCore) : ViewModel(){

    enum class Type{
        PULL, PUSH
    }

    data class Event(
        val type: Type,
        val userId: User.Id,
        val listId: UserList.Id
    )

    @Suppress("UNCHECKED_CAST")
    class Factory(val miCore: MiCore) : ViewModelProvider.Factory{
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UserListPullPushUserViewModel(miCore) as T
        }
    }

    val account = MutableLiveData<Account>(miCore.getCurrentAccount().value)

    private val subject = PublishSubject.create<Event>()
    val pullPushEvent: Observable<Event> = subject


    fun toggle(userList: UserList, userId: User.Id){
        val account = miCore.getCurrentAccount().value
        if(account == null){
            Log.w(this.javaClass.simpleName, "Accountを見つけることができなかった処理を中断する")
            return
        }
        val misskeyAPI = miCore.getMisskeyAPI(account)

        val hasUserInUserList = userList.userIds.contains(userId)
        val api = if(hasUserInUserList){
            // pull
            misskeyAPI::pullUserFromList
        }else{
            // push
            misskeyAPI::pushUserToList
        }

        val type = if(hasUserInUserList){
            Type.PULL
        }else{
            Type.PUSH
        }

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                api.invoke(ListUserOperation(i = account.getI(miCore.getEncryption()), listId = userList.id.userListId, userId = userId.id))
                    .throwIfHasError()
            }.onSuccess {
                subject.onNext(
                    Event(type = type, userId = userId, listId = userList.id)
                )
            }.onFailure {
                Log.d(this.javaClass.simpleName, "ユーザーを${type}するのに失敗した")
            }
        }

    }

}