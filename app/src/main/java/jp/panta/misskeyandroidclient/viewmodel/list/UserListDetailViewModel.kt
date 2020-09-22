package jp.panta.misskeyandroidclient.viewmodel.list

import android.util.Log
import androidx.lifecycle.*
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.ReplaySubject
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.AccountNotFoundException
import jp.panta.misskeyandroidclient.model.list.ListId
import jp.panta.misskeyandroidclient.model.list.ListUserOperation
import jp.panta.misskeyandroidclient.model.list.UpdateList
import jp.panta.misskeyandroidclient.model.list.UserList
import jp.panta.misskeyandroidclient.model.users.RequestUser
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.users.UserViewData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.ConcurrentLinkedDeque

class UserListDetailViewModel(
    val accountId: Long,
    val listId: String,
    val miCore: MiCore
) : ViewModel(){

    @Suppress("UNCHECKED_CAST")
    class Factory(val accountId: Long, val listId: String, private val miCore: MiCore) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return UserListDetailViewModel(accountId, listId, miCore) as T
        }
    }

    private val tag = this.javaClass.simpleName




    private val mUserMap = LinkedHashMap<String, UserViewData>()

    //private val mPublisher = UserListEventStore(misskeyAPI, account).getEventStream()
    val updateEvents = ConcurrentLinkedDeque<UserListEvent>()


    private val mAccount = MutableLiveData<Account>()
    private val mUserList = MutableLiveData<UserList>()

    private val mListUsers = MutableLiveData<List<UserViewData>>()

    val userList: LiveData<UserList> = mUserList

    val listUsers: LiveData<List<UserViewData>> = mListUsers

    init{

        viewModelScope.launch(Dispatchers.IO){
            try{
                mAccount.postValue(miCore.getAccount(accountId))
            }catch(e: AccountNotFoundException){
                Log.e(tag, "指定されたaccountId:${accountId}のアカウントを発見することができませんでした。", e)
            }
        }

        mAccount.observeForever {
            load()
        }

        mUserList.observeForever { ul ->
            mAccount.value?.let{ ac ->
                loadUsers(ac, ul.userIds)
            }
        }
    }

    fun load(){
        val account = mAccount.value
        if(account == null){
            Log.i(tag, "#load アカウントがまだ読み込めていません。")
            return
        }

        miCore.getMisskeyAPI(account).showList(
            ListId(
                i = account.getI(miCore.getEncryption())!!,
                listId = listId
            )
        ).enqueue(object : Callback<UserList>{
            override fun onResponse(call: Call<UserList>, response: Response<UserList>) {
                val ul = response.body()?: return
                mUserList.postValue(ul)
                //loadUsers(account, ul.userIds)
            }

            override fun onFailure(call: Call<UserList>, t: Throwable) {
                Log.e(tag, "load user list error, listId:$listId", t)
            }
        })
    }

    fun updateName(name: String){
        val account = mAccount.value
        if(account == null){
            Log.i(tag, "#load アカウントがまだ読み込めていません。")
            return
        }

        miCore.getMisskeyAPI(account).updateList(
            UpdateList(
                i = account.getI(miCore.getEncryption())!!,
                listId = listId,
                name = name
            )
        ).enqueue(object : Callback<Unit>{
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if(response.code() in 200 until 300){
                    updateEvents.add(
                        UserListEvent(userListId = listId, account = account, type = UserListEvent.Type.UPDATED_NAME)
                    )
                    load()
                }
            }
            override fun onFailure(call: Call<Unit>, t: Throwable) {

            }
        })
    }

    fun pushUser(userId: String){
        val account = mAccount.value
        if(account == null){
            Log.i(tag, "#load アカウントがまだ読み込めていません。")
            return
        }

        miCore.getMisskeyAPI(account).pushUserToList(
            ListUserOperation(
                i = account.getI(miCore.getEncryption())!!,
                listId = listId,
                userId = userId
            )
        ).enqueue(object : Callback<Unit>{
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if(response.code() in 200 until 300){
                    onPushedUser(account, userId)
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Log.d(tag, "push user error", t)
            }
        })
    }


    fun pullUser(userId: String){
        val account = mAccount.value
        if(account == null){
            Log.i(tag, "#load アカウントがまだ読み込めていません。")
            return
        }

        miCore.getMisskeyAPI(account).pullUserFromList(
            ListUserOperation(
                i = account.getI(miCore.getEncryption())!!,
                listId = listId,
                userId = userId
            )
        ).enqueue(object : Callback<Unit>{
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if(response.code() in 200 until 300){
                    onPulledUser(account, userId)
                }else{
                    Log.d(tag, "pull user failure: $response")
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Log.d(tag, "pull user error", t)
            }
        })
    }
    private fun loadUsers(account: Account, userIds: List<String>){

        Log.d(tag, "load users $userIds")
        mUserMap.clear()

        val listUserViewDataList = userIds.map{ userId ->
            UserViewData(userId).apply{
                miCore.getMisskeyAPI(account).showUser(
                    RequestUser(
                    i = account.getI(miCore.getEncryption())!!,
                    userId = userId
                )).enqueue(this.accept)
            }
        }

        mUserMap.putAll(
            listUserViewDataList.map{
                it.userId to it
            }
        )
        mListUsers.postValue(mUserMap.values.toList())
    }

    private fun onPushedUser(account: Account, userId: String){
        val newUser = UserViewData(userId)
        mUserMap[userId] = newUser
        loadAndPutUser(account, newUser)
        adaptUsers()

        updateEvents.add(UserListEvent(
            account = account,
            userListId = listId,
            userId = userId,
            type = UserListEvent.Type.PUSH_USER
        ))
    }

    private fun onPulledUser(account: Account, userId: String){
        mUserMap.remove(userId)
        adaptUsers()

        updateEvents.add(UserListEvent(
            account = account,
            userListId = listId,
            userId = userId,
            type = UserListEvent.Type.PULL_USER
        ))
    }


    private fun loadAndPutUser(account: Account, user: UserViewData){
        miCore.getMisskeyAPI(account).showUser(
            RequestUser(
                i = account.getI(miCore.getEncryption()),
                userId = user.userId
            )).enqueue(user.accept)
    }

    private fun adaptUsers(){
        mListUsers.postValue(mUserMap.values.toList())
    }

}