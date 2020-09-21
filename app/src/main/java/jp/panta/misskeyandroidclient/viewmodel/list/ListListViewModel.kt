package jp.panta.misskeyandroidclient.viewmodel.list

import android.util.Log
import androidx.lifecycle.*
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.I
import jp.panta.misskeyandroidclient.model.account.page.Page
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.page.Pageable
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.list.UpdateList
import jp.panta.misskeyandroidclient.model.list.UserList
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.util.eventbus.EventBus
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.LinkedHashMap

class ListListViewModel(
    val miCore: MiCore
) : ViewModel(){

    @Suppress("UNCHECKED_CAST")
    class Factory(val miCore: MiCore) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ListListViewModel( miCore) as T
        }
    }

    companion object{
        private const val TAG = "ListListViewModel"
    }

    val encryption = miCore.getEncryption()

    var account: Account? = null
    val userListList = MediatorLiveData<List<UserList>>().apply{
        addSource(miCore.getCurrentAccount()){
            account = it
            loadListList(it)
        }
    }

    val pagedUserList = MediatorLiveData<Set<UserList>>().apply{
        addSource(userListList){ userLists ->
            userLists.filter{ ul ->
                account?.pages?.any {
                    (it.pageable() as? Pageable.UserListTimeline)?.listId == ul.id
                }?:false
            }
        }
    }

    private val mUserListIdMap = LinkedHashMap<String, UserList>()


    val showUserDetailEvent = EventBus<UserList>()

    //private val mPublisher = UserListEventStore(misskeyAPI, account).getEventStream()

    init{
        //mPublisher.subscribe(UserListEventObserver())
    }


    fun loadListList(account: Account? = this.account){
        val i = account?.getI(encryption)
            ?: return
        miCore.getMisskeyAPI(account).userList(I(i)).enqueue(object : Callback<List<UserList>>{
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



    fun showUserListDetail(userList: UserList?){
        userList?.let{ ul ->
            showUserDetailEvent.event = ul
        }
    }

    fun toggleTab(userList: UserList?){
        userList?.let{ ul ->
            val exPage = account?.pages?.firstOrNull {
                val pageable = it.pageable()
                if(pageable is Pageable.UserListTimeline){
                    pageable.listId == ul.id
                }else{
                    false
                }
            }
            if(exPage == null && account != null){
                val page = Page(account!!.accountId, ul.name, pageable =  Pageable.UserListTimeline(ul.id), weight = 0)
                miCore.addPageInCurrentAccount(page)
            }else if(exPage != null){
                miCore.removePageInCurrentAccount(exPage)
            }
        }
    }

    inner class UserListEventObserver : Observer<UserListEvent>{
        override fun onComplete() = Unit
        override fun onError(e: Throwable){
            Log.e("UserListViewModel", "error", e)
        }

        override fun onSubscribe(d: Disposable) = Unit

        override fun onNext(t: UserListEvent) {
            when(t.type){
                UserListEvent.Type.UPDATED_NAME ->{
                    onUserListUpdated(t.userListId, t.name!!)
                }
                UserListEvent.Type.CREATE ->{
                    mUserListIdMap[t.userListId] = t.userList!!
                    userListList.postValue(mUserListIdMap.values.toList())
                }
                else ->{

                }
            }
        }
    }
}