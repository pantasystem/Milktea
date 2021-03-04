package jp.panta.misskeyandroidclient.viewmodel.users.selectable

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.api.users.RequestUser
import jp.panta.misskeyandroidclient.api.users.UserDTO
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.users.UserViewData


class SelectedUserViewModel(
    val miCore: MiCore,
    val selectableSize: Int,
    val exSelectedUserIds: List<User.Id> = emptyList(),
    val exSelectedUsers: List<User> = emptyList()
) : ViewModel(){

    @Suppress("UNCHECKED_CAST")
    class Factory(
        val miCore: MiCore,
        val selectableSize: Int,
        val selectedUserIds: List<User.Id>?,
        val selectedUsers: List<User>?
    ) : ViewModelProvider.Factory{
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SelectedUserViewModel(
                miCore,
                selectableSize,
                selectedUserIds?: emptyList(),
                selectedUsers?: emptyList()
            ) as T
        }
    }


    data class ChangedDiffResult(
        val selected: List<User.Id>,
        val added: List<User.Id>,
        val removed: List<User.Id>
    )

    private val mSelectedUserIdUserMap: HashMap<User.Id, UserViewData>

    val selectedUsers = MediatorLiveData<List<UserViewData>>()

    val selectedUserIds = MediatorLiveData<Set<User.Id>>().apply{
        addSource(selectedUsers){
            value = it.map{ uv ->
                uv.userId
            }.toSet()
        }
    }

    val isSelectable = MediatorLiveData<Boolean>().apply{
        addSource(selectedUserIds){
            value = it.size <= selectableSize
        }
    }


    init{
        val usersMap = HashMap<User.Id, UserViewData>()

        val srcUser = exSelectedUsers.map{
            it.id to UserViewData(it, miCore, viewModelScope)
        }

        usersMap.putAll(srcUser)

        val srcUserId = exSelectedUserIds.mapNotNull{

            if(usersMap.containsKey(it)){
                null
            }else{
                it to UserViewData(it, miCore, viewModelScope)
            }

        }

        usersMap.putAll(srcUserId)

        mSelectedUserIdUserMap = LinkedHashMap(usersMap)
        selectedUsers.postValue(mSelectedUserIdUserMap.values.toList())
    }

    fun selectUser(user: User?){
        user?: return
        synchronized(mSelectedUserIdUserMap){
            mSelectedUserIdUserMap[user.id] = UserViewData(user, miCore, viewModelScope)
            selectedUsers.postValue(mSelectedUserIdUserMap.values.toList())
        }
    }

    fun unSelectUser(user: User?){
        user?: return
        synchronized(mSelectedUserIdUserMap){
            mSelectedUserIdUserMap.remove(user.id)
            selectedUsers.postValue(mSelectedUserIdUserMap.values.toList())
        }
    }

    fun toggleSelectUser(user: User?){
        user?: return

        synchronized(mSelectedUserIdUserMap){
            if(mSelectedUserIdUserMap.containsKey(user.id)){
                unSelectUser(user)
            }else{
                selectUser(user)
            }
        }
    }

    fun isSelectedUser(user: User?): Boolean{
        user?: return false
        return synchronized(mSelectedUserIdUserMap){
            mSelectedUserIdUserMap.containsKey(user.id)
        }
    }

    fun getSelectedUserIdsChangedDiff(): ChangedDiffResult {
        val selectedBeforeIds = exSelectedUserIds.toSet()
        val selectedBeforeUsers = exSelectedUsers.map{
            it.id
        }.toSet()
        val exSelected = HashSet<User.Id>().apply{
            addAll(selectedBeforeIds)
            addAll(selectedBeforeUsers)
        }

        val selected = selectedUsers.value?.map{
            it.userId
        }?: emptyList()



        val added = selected.filter{ s ->
            !exSelected.contains(s)
        }

        val removed = exSelected.filter{ ex ->
            !selected.contains(ex)
        }
        return ChangedDiffResult(selected.toList(), added, removed)
    }
}