package jp.panta.misskeyandroidclient.ui.list.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import net.pantasystem.milktea.api.misskey.list.ListUserOperation
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.list.UserList
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject

@HiltViewModel
class UserListPullPushUserViewModel @Inject constructor(
    val accountStore: AccountStore,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val encryption: Encryption
) : ViewModel() {

    enum class Type {
        PULL, PUSH
    }

    data class Event(
        val type: Type,
        val userId: User.Id,
        val listId: UserList.Id
    )



    val account = MutableLiveData<Account>(accountStore.currentAccount)

//    private val subject = PublishSubject.create<Event>()
//    val pullPushEvent: Observable<Event> = subject
    private val _pullPushEvent = MutableSharedFlow<Event>()
    val pullPushEvent: SharedFlow<Event> = _pullPushEvent


    fun toggle(userList: UserList, userId: User.Id) {
        val account = accountStore.currentAccount
        if (account == null) {
            Log.w(this.javaClass.simpleName, "Accountを見つけることができなかった処理を中断する")
            return
        }
        val misskeyAPI = misskeyAPIProvider.get(account)

        val hasUserInUserList = userList.userIds.contains(userId)
        val api = if (hasUserInUserList) {
            // pull
            misskeyAPI::pullUserFromList
        } else {
            // push
            misskeyAPI::pushUserToList
        }

        val type = if (hasUserInUserList) {
            Type.PULL
        } else {
            Type.PUSH
        }

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                api.invoke(
                    ListUserOperation(
                        i = account.getI(encryption),
                        listId = userList.id.userListId,
                        userId = userId.id
                    )
                )
                    .throwIfHasError()
            }.onSuccess {
                _pullPushEvent.tryEmit(Event(type = type, userId = userId, listId = userList.id))
            }.onFailure {
                Log.d(this.javaClass.simpleName, "ユーザーを${type}するのに失敗した")
            }
        }

    }

}