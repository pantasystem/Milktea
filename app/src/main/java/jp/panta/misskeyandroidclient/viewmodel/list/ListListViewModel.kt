package jp.panta.misskeyandroidclient.viewmodel.list

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.I
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.list.UpdateList
import jp.panta.misskeyandroidclient.model.list.UserList
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.LinkedHashMap

class ListListViewModel(
    val accountRelation: AccountRelation,
    val misskeyAPI: MisskeyAPI,
    val encryption: Encryption
) : ViewModel(){

    @Suppress("UNCHECKED_CAST")
    class Factory(val accountRelation: AccountRelation, val miCore: MiCore) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ListListViewModel(accountRelation, miCore.getMisskeyAPI(accountRelation)!!, miCore.getEncryption()) as T
        }
    }

    companion object{
        private const val TAG = "ListListViewModel"
    }
    val userListList = MutableLiveData<List<UserList>>()

    private val mUserListIdMap = LinkedHashMap<String, UserList>()

    val updateUserListEvent = EventBus<UserList>()

    val showUserDetailEvent = EventBus<UserList>()


    fun loadListList(){
        val i = accountRelation.getCurrentConnectionInformation()?.getI(encryption)
            ?: return
        misskeyAPI.userList(I(i)).enqueue(object : Callback<List<UserList>>{
            override fun onResponse(
                call: Call<List<UserList>>,
                response: Response<List<UserList>>
            ) {
                val userListMap = response.body()?.map{
                    it.id to it
                }?.toMap()?: emptyMap()
                mUserListIdMap.clear()
                mUserListIdMap.putAll(userListMap)

                userListList.postValue(mUserListIdMap.values.toList())
            }

            override fun onFailure(call: Call<List<UserList>>, t: Throwable) {
                Log.d(TAG, "loadListList error", t)
            }
        })
    }

    fun updateUserList(userList: UserList?, name: String?){
        name?: return
        userList?: return
        misskeyAPI.updateList(
            UpdateList(
                i = accountRelation.getCurrentConnectionInformation()?.getI(encryption)!!,
                name = name,
                listId = userList.id
            )
        ).enqueue(object : Callback<Unit>{
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if(response.code() in 200 until 300){

                    onUserListUpdated(userList.id, name)
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Log.e(TAG, "update user list error", t)
            }
        })
    }

    /**
     * 他Activityで変更を加える場合onActivityResultで呼び出し変更を適応する
     */
    fun onUserListUpdated(listId: String, name: String){
        val updated = mUserListIdMap[listId]?.copy(name = name)?: return
        mUserListIdMap[listId] = updated
        userListList.postValue(mUserListIdMap.values.toList())
    }

    /**
     * 他Activity等でUserListを正常に作成できた場合onActivityResultで呼び出し変更を適応する
     */
    fun onUserListCreated(listId: String, createdAt: Date, name: String, userIds: List<String>){
        val createdUser = UserList(
            id = listId,
            createdAt = createdAt,
            name = name,
            userIds = userIds
        )
        mUserListIdMap[createdUser.id] = createdUser
        userListList.postValue(mUserListIdMap.values.toList())
    }

    fun setUpdateUserList(userList: UserList?){
        updateUserListEvent.event = userList
    }

    fun addToTab(userList: UserList?){

    }

    fun showUserListDetail(userList: UserList?){
        userList?.let{ ul ->
            showUserDetailEvent.event = ul
        }
    }

}