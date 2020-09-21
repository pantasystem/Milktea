package jp.panta.misskeyandroidclient.viewmodel.list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.list.*
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserListOperateViewModel(
    val miCore: MiCore,
    val userListEventStore: UserListEventStore
    //val account: Account,
    //val misskeyAPI: MisskeyAPI,
    //val miCore.getEncryption(): Encryption
) : ViewModel(){

    @Suppress("UNCHECKED_CAST")
    class Factory(val account: Account, val miCore: MiCore) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {

            return UserListOperateViewModel(account, miCore.getMisskeyAPI(account), miCore.getEncryption()) as T
        }
    }

    private val tag = this.javaClass.simpleName

    
    val updateUserListEvent = EventBus<UserList>()

    init{
        miCore.getCurrentAccount()
    }


    fun pushUser(account: Account, userList: UserList, userId: String){
        miCore.getMisskeyAPI(account).pushUserToList(
            ListUserOperation(
                i = account.getI(miCore.getEncryption())!!,
                listId = userList.id,
                userId = userId
            )
        ).enqueue(object : Callback<Unit>{
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if(response.code() in 200 until 300){
                    userListEventStore.onPushUser(userList.id, userId)
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Log.d(tag, "push user error", t)
            }
        })
    }

    fun pullUser(userListId: String, userId: String){
        misskeyAPI.pullUserFromList(
            ListUserOperation(
                i = account.getI(miCore.getEncryption())!!,
                listId = userListId,
                userId = userId
            )
        ).enqueue(object : Callback<Unit>{
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if(response.code() in 200 until 300){
                    userListEventStore.onPullUser(userListId, userId)
                }else{
                    Log.d(tag, "pull user failure: $response")
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Log.d(tag, "pull user error", t)
            }
        })
    }

    fun rename(listId: String, name: String){
        Log.d(tag, "update listId:$listId, name:$name")
        misskeyAPI.updateList(
            UpdateList(
                i = account.getI(miCore.getEncryption())!!,
                name = name,
                listId = listId
            )
        ).enqueue(object : Callback<Unit>{
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                Log.d(tag, "update list, response:$response")
                if(response.code() in 200 until 300){
                    userListEventStore.onUpdateUserList(listId, name)
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Log.d(tag, "rename userList error", t)
            }
        })
    }

    fun create(createUserList: CreateList){
        misskeyAPI.createList(
            createUserList
        ).enqueue(object : Callback<UserList>{
            override fun onResponse(call: Call<UserList>, response: Response<UserList>) {
                if(response.code() in 200 until 300){
                    userListEventStore.onCreateUserList(response.body()!!)
                }
            }

            override fun onFailure(call: Call<UserList>, t: Throwable) {
                Log.d(tag, "user list create error", t)
            }
        })
    }

    fun delete(userList: UserList){
        misskeyAPI.deleteList(ListId(
            i = account.getI(miCore.getEncryption())!!,
            listId = userList.id
        )).enqueue(
            object : Callback<Unit>{
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if(response.code() in 200 until 300){
                        userListEventStore.onDeleteUserList(userList)
                    }
                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    Log.d(tag, "user list delete error", t)
                }
            }
        )
    }

    fun showUserListUpdateDialog(userList: UserList?){
        userList?.let{ ul ->
            updateUserListEvent.event = ul
        }
    }

}