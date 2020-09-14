package jp.panta.misskeyandroidclient.viewmodel.users.selectable

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import jp.panta.misskeyandroidclient.model.users.RequestUser
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.users.UserViewData
import jp.panta.misskeyandroidclient.viewmodel.users.UsersLiveData
import jp.panta.misskeyandroidclient.viewmodel.users.search.SearchUserViewModel


class SelectedUserViewModel(
    val miCore: MiCore,
    val selectableSize: Int,
    val exSelectedUserIds: List<String> = emptyList(),
    val exSelectedUsers: List<User> = emptyList()
) : ViewModel(){

    @Suppress("UNCHECKED_CAST")
    class Factory(
        val miCore: MiCore,
        val selectableSize: Int,
        val selectedUserIds: List<String>?,
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
        val selected: List<String>,
        val added: List<String>,
        val removed: List<String>
    )

    private val mSelectedUserIdUserMap = LinkedHashMap<String, UserViewData>()

    val selectedUsers = UsersLiveData().apply{
        addSource(miCore.getCurrentAccount()){
            it?.let{ ar ->
                setMainCapture(miCore.getMainCapture(ar))
            }
        }
    }
    val selectedUserIds = MediatorLiveData<Set<String>>().apply{
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
        val usersMap = HashMap<String, UserViewData>()

        val misskeyAPI = miCore.getMisskeyAPI(miCore.getCurrentAccount().value!!)

        val srcUser = exSelectedUsers.map{
            it.id to UserViewData(it)
        }

        usersMap.putAll(srcUser)

        val srcUserId = exSelectedUserIds.mapNotNull{
            val call = misskeyAPI.showUser(RequestUser(
                i = miCore.getCurrentAccount().value?.getI(miCore.getEncryption()),
                userId = it
            ))
            if(usersMap.containsKey(it)){
                null
            }else{
                it to UserViewData(it).apply{
                    call?.enqueue(accept)
                }
            }

        }

        usersMap.putAll(srcUserId)


    }

    fun selectUser(user: User?){
        user?: return
        synchronized(mSelectedUserIdUserMap){
            mSelectedUserIdUserMap[user.id] = UserViewData(user)
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
        val exSelected = HashSet<String>().apply{
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