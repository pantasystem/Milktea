package jp.panta.misskeyandroidclient.viewmodel.list

import android.util.Log
import androidx.lifecycle.*
import jp.panta.misskeyandroidclient.model.Encryption
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
    val accountRelation: AccountRelation,
    val listId: String,
    val misskeyAPI: MisskeyAPI,
    val encryption: Encryption
) : ViewModel(){

    @Suppress("UNCHECKED_CAST")
    class Factory(val accountRelation: AccountRelation, val listId: String, private val miCore: MiCore) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return UserListDetailViewModel(accountRelation, listId, miCore.getMisskeyAPI(accountRelation)!!, miCore.getEncryption()) as T
        }
    }

    private val tag = this.javaClass.simpleName.toString()


    val userList = MutableLiveData<UserList>()
    val listUsers = MutableLiveData<List<ListUserViewData>>()

    private val mUserMap = LinkedHashMap<String, ListUserViewData>()


    fun load(){
        misskeyAPI.showList(
            ListId(
                i = accountRelation.getCurrentConnectionInformation()?.getI(encryption)!!,
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

    fun updateName(name: String){
        misskeyAPI.updateList(
            UpdateList(
                i = accountRelation.getCurrentConnectionInformation()?.getI(encryption)!!,
                listId = listId,
                name = name
            )
        ).enqueue(object : Callback<Unit>{
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if(response.code() in 200 until 300){
                    val exUserList = userList.value?: return
                    userList.postValue(
                        exUserList.copy(name = name)
                    )
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Log.e(tag, "update error", t)
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
                    i = accountRelation.getCurrentConnectionInformation()?.getI(encryption)!!,
                    userId = userId
                ))
            }
        }

        mUserMap.putAll(
            listUserViewDataList.map{
                it.userId to it
            }
        )
        listUsers.postValue(mUserMap.values.toList())
    }


    fun pushUser(userId: String){
        misskeyAPI.pushUserToList(
            ListUserOperation(
                i = accountRelation.getCurrentConnectionInformation()?.getI(encryption)!!,
                userId = userId,
                listId = listId
            )
        ).enqueue(object : Callback<Unit>{
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if(response.code() in 200 until 300){
                    val viewData = ListUserViewData(userId)
                    mUserMap[userId] = viewData
                    misskeyAPI.showUser(
                        RequestUser(
                            i = accountRelation.getCurrentConnectionInformation()?.getI(encryption)!!,
                            userId = userId
                        )
                    ).enqueue(viewData.accept)
                    listUsers.postValue(mUserMap.values.toList())
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Log.e(tag, "push user error", t)
            }
        })

    }

    fun pullUser(user: User?){
        user?: return
        misskeyAPI.pullUserFromList(
            ListUserOperation(
                i = accountRelation.getCurrentConnectionInformation()?.getI(encryption)!!,
                userId = user.id,
                listId = listId
            )
        ).enqueue(object : Callback<Unit>{
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if(response.code() in 200 until 300){
                    mUserMap.remove(user.id)
                    listUsers.postValue(mUserMap.values.toList())
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Log.e(tag, "pull user error", t)
            }
        })
    }

}