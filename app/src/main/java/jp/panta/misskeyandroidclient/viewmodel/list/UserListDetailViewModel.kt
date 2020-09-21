package jp.panta.misskeyandroidclient.viewmodel.list

import android.util.Log
import androidx.lifecycle.*
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.list.ListId
import jp.panta.misskeyandroidclient.model.list.ListUserOperation
import jp.panta.misskeyandroidclient.model.list.UpdateList
import jp.panta.misskeyandroidclient.model.list.UserList
import jp.panta.misskeyandroidclient.model.users.RequestUser
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserListDetailViewModel(
    val account: Account,
    val listId: String,
    val misskeyAPI: MisskeyAPI,
    val encryption: Encryption
) : ViewModel(){

    @Suppress("UNCHECKED_CAST")
    class Factory(val account: Account, val listId: String, private val miCore: MiCore) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return UserListDetailViewModel(account, listId, miCore.getMisskeyAPI(account), miCore.getEncryption()) as T
        }
    }

    private val tag = this.javaClass.simpleName


    val userList = MutableLiveData<UserList>()
    val listUsers = MutableLiveData<List<ListUserViewData>>()

    private val mUserMap = LinkedHashMap<String, ListUserViewData>()

    //private val mPublisher = UserListEventStore(misskeyAPI, account).getEventStream()

    init{
        //mPublisher.subscribe(UserListObserver())
    }

    fun load(){
        misskeyAPI.showList(
            ListId(
                i = account.getI(encryption)!!,
                listId = listId
            )
        ).enqueue(object : Callback<UserList>{
            override fun onResponse(call: Call<UserList>, response: Response<UserList>) {
                val ul = response.body()?: return
                userList.postValue(ul)
                loadUsers(ul.userIds)
            }

            override fun onFailure(call: Call<UserList>, t: Throwable) {
                Log.e(tag, "load user list error, listId:$listId", t)
            }
        })
    }


    private fun loadUsers(userIds: List<String>){

        Log.d(tag, "load users $userIds")
        mUserMap.clear()

        val listUserViewDataList = userIds.map{ userId ->
            ListUserViewData(userId).apply{
                misskeyAPI.showUser(
                    RequestUser(
                    i = account.getI(encryption)!!,
                    userId = userId
                )).enqueue(this.accept)
            }
        }

        mUserMap.putAll(
            listUserViewDataList.map{
                it.userId to it
            }
        )
        listUsers.postValue(mUserMap.values.toList())
    }


    inner class UserListObserver : Observer<UserListEvent>{
        override fun onNext(t: UserListEvent) {
            when(t.type){
                UserListEvent.Type.PUSH_USER ->{
                    t.userId?.let{ id ->
                        val newUser = ListUserViewData(id)
                        mUserMap[id] = newUser
                        loadAndPutUser(newUser)
                        adaptUsers()
                    }
                }
                UserListEvent.Type.PULL_USER ->{
                    t.userId?.let{ id ->
                        mUserMap.remove(id)
                        adaptUsers()
                    }
                }
                UserListEvent.Type.UPDATED_NAME ->{
                    userList.postValue(userList.value?.copy(name = t.name!!))
                }

                else -> {

                }
            }
        }

        override fun onComplete() = Unit
        override fun onSubscribe(d: Disposable) = Unit


        override fun onError(e: Throwable) {
            Log.e(tag, "error", e)
        }

    }

    private fun loadAndPutUser(user: ListUserViewData){
        misskeyAPI.showUser(
            RequestUser(
                i = account.getI(encryption),
                userId = user.userId
            )).enqueue(user.accept)
    }

    private fun adaptUsers(){
        listUsers.postValue(mUserMap.values.toList())
    }

}