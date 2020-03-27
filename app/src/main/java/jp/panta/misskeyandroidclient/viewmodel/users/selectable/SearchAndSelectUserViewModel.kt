package jp.panta.misskeyandroidclient.viewmodel.users.selectable

import android.util.Log
import androidx.lifecycle.*
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.users.RequestUser
import jp.panta.misskeyandroidclient.model.users.SearchByUserAndHost
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.users.UserViewData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchAndSelectUserViewModel(
    val accountRelation: AccountRelation,
    val misskeyAPI: MisskeyAPI,
    val encryption: Encryption,
    val selectableSize: Int,
    val selectedUserIds: List<String> = emptyList()
) : ViewModel(){

    @Suppress("UNCHECKED_CAST")
    class Factory(val accountRelation: AccountRelation, val miCore: MiCore, val selectableSize: Int, val selectedUserIds: List<String>) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            val misskeyAPI = miCore.getMisskeyAPI(accountRelation)!!
            return SearchAndSelectUserViewModel(
                accountRelation,
                misskeyAPI,
                miCore.getEncryption(),
                selectableSize,
                selectedUserIds
            ) as T
        }
    }

    data class ChangeDiff(
        val selected: List<String>,
        val added: List<String>,
        val removed: List<String>
    )

    companion object{
        private const val TAG = "SearchAndSelectUserVM"
    }

    private val mSearchByUserAndHost = SearchByUserAndHost(misskeyAPI)

    val userName = MutableLiveData<String>()
    val host = MutableLiveData<String>()


    val searchResultUsers = MediatorLiveData<List<SelectableUserViewData>>()

    private val mSearchResultTargetUsersMap = LinkedHashMap<String, SelectableUserViewData>()
    private val mSelectedUsersMap = HashMap<String, SelectableUserViewData>()

    val selectedUsers = MutableLiveData<List<SelectableUserViewData>>()

    val isSelectable = Transformations.map(searchResultUsers){
        mSelectedUsersMap.size  < selectableSize
    }

    val selectedUsersSize = Transformations.map(selectedUsers){
        it.size
    }


    init{
        searchResultUsers.addSource(userName){
            search()
        }
        searchResultUsers.addSource(host){
            search()
        }

        mSelectedUsersMap.putAll(
            selectedUserIds.map{ userId ->
                val uvd = UserViewData(userId)
                accountRelation.getCurrentConnectionInformation()?.getI(encryption)?.let{ i ->
                    uvd.setApi(i, misskeyAPI)
                }
                userId to SelectableUserViewData(uvd, true)
            }
        )

        selectedUsers.postValue(mSelectedUsersMap.values.toList())

    }

    fun search(){
        Log.d(TAG, "検索を開始します")

        val userName = this.userName.value?: return
        val host = this.host.value


        val request = RequestUser(
            i = accountRelation.getCurrentConnectionInformation()?.getI(encryption)!!,
            userName = userName,
            userId = null,
            host = host
        )

        mSearchByUserAndHost.search(request).enqueue(object : Callback<List<User>>{
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                synchronized(mSearchResultTargetUsersMap){
                    mSearchResultTargetUsersMap.clear()
                    mSearchResultTargetUsersMap.putAll(response.body()?.map{
                        it.id to SelectableUserViewData(
                            UserViewData(it),
                            mSelectedUsersMap[it.id] != null
                        )
                    }?.toMap()?: emptyMap())
                }
                synchronized(mSearchResultTargetUsersMap){
                    searchResultUsers.postValue(
                        mSearchResultTargetUsersMap.values.toList()
                    )
                }

            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Log.e(TAG, "search and select user error", t)
            }
        })
    }


    fun toggleSelect(selectableUser: SelectableUserViewData?){
        selectableUser?: return
        val user = synchronized(mSearchResultTargetUsersMap){
            mSearchResultTargetUsersMap[selectableUser.user.userId]
        }?: return

        val toggled = user.copy(isSelected = !user.isSelected)

        synchronized(mSearchResultTargetUsersMap){
            mSearchResultTargetUsersMap[toggled.user.userId] = toggled
        }

        synchronized(mSelectedUsersMap){
            if(toggled.isSelected){
                mSelectedUsersMap[toggled.user.userId] = toggled
            }else{
                mSelectedUsersMap.remove(toggled.user.userId)
            }
        }

        synchronized(mSelectedUsersMap){
            selectedUsers.postValue(mSelectedUsersMap.values.toList())
        }

        synchronized(mSearchResultTargetUsersMap){
            searchResultUsers.postValue(mSearchResultTargetUsersMap.values.toList())
        }

    }

    fun getSelectedUserIdsChangedDiff(): ChangeDiff{
        val exSelected = selectedUserIds.toSet()

        val selected = (selectedUsers.value?.map{
            it.user.userId
        }?: emptyList()).toSet()

        val added = selected.filter{ s ->
            !exSelected.contains(s)
        }

        val removed = exSelected.filter{ ex ->
            !selected.contains(ex)
        }
        return ChangeDiff(selected.toList(), added, removed)
    }
}