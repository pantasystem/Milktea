package jp.panta.misskeyandroidclient.ui.users.viewmodel.selectable

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.ui.users.viewmodel.UserViewData
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import net.pantasystem.milktea.model.user.User
import java.io.Serializable

@OptIn(ExperimentalCoroutinesApi::class)
class SelectedUserViewModel(
    val miCore: MiCore,
    val selectableSize: Int,
    private val exSelectedUserIds: List<User.Id> = emptyList(),
    private val exSelectedUsers: List<User> = emptyList()
) : ViewModel(){

    @Suppress("UNCHECKED_CAST")
    class Factory(
        val miCore: MiCore,
        private val selectableSize: Int,
        private val selectedUserIds: List<User.Id>?,
        val selectedUsers: List<User>?
    ) : ViewModelProvider.Factory{
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
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
        val removed: List<User.Id>,
        val selectedUsers: List<User>
    ) : Serializable

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private val mSelectedUserIdUserMap: HashMap<User.Id, UserViewData>

    val selectedUsers = MediatorLiveData<List<UserViewData>>()

    val selectedUserIds = MediatorLiveData<Set<User.Id>>().apply{
        addSource(selectedUsers){
            value = it.mapNotNull{ uv ->
                uv.userId
            }.toSet()
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

    private fun selectUser(user: User?){
        user?: return
        synchronized(mSelectedUserIdUserMap){
            mSelectedUserIdUserMap[user.id] = UserViewData(user, miCore, viewModelScope)
            selectedUsers.postValue(mSelectedUserIdUserMap.values.toList())
        }
    }

    private fun unSelectUser(user: User?){
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

        val selectedUsers = selectedUsers.value?.mapNotNull {
            it.user.value
        } ?: emptyList<User>()



        val added = selected.filter{ s ->
            !exSelected.contains(s)
        }

        val removed = exSelected.filter{ ex ->
            !selected.contains(ex)
        }
        return ChangedDiffResult(selected.filterNotNull().toList(), added.filterNotNull(), removed, selectedUsers)
    }
}