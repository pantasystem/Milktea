package jp.panta.misskeyandroidclient.viewmodel.list

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.list.ListUserOperation
import jp.panta.misskeyandroidclient.model.list.UserList
import jp.panta.misskeyandroidclient.viewmodel.MiCore
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
        val userId: String,
        val listId: String
    )

    val account = MutableLiveData<Account>(miCore.getCurrentAccount().value)

    private val subject = PublishSubject.create<Event>()
    val pullPushEvent: Observable<Event> = subject


    fun toggle(userList: UserList, userId: String){
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

        api.invoke(
            ListUserOperation(i = account.getI(miCore.getEncryption())!!, listId = userList.id, userId = userId)
        ).enqueue(object : Callback<Unit>{
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if(response.code() in 200 until 300){

                    subject.onNext(
                        Event(type = type, userId = userId, listId = userList.id)
                    )
                }else{
                    Log.d(this.javaClass.simpleName, "ユーザーを${type}するのに失敗した")
                }
            }
            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Log.d(this.javaClass.simpleName, "ユーザーを${type}するのに失敗した")
            }
        })
    }

}