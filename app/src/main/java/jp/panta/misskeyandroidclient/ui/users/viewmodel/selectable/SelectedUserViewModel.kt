package jp.panta.misskeyandroidclient.ui.users.viewmodel.selectable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.model.user.User
import java.io.Serializable

data class SelectedUserUiState(
    val selectedUserIds: Set<User.Id>
) {
    fun toggle(userId: User.Id): SelectedUserUiState {

        return copy(
            selectedUserIds = selectedUserIds.toMutableSet().also {
                if (it.contains(userId)) {
                    it.remove(userId)
                } else {
                    it.add(userId)
                }
            }
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SelectedUserViewModel(
    val miCore: MiCore,
    private val exSelectedUserIds: List<User.Id> = emptyList(),
    private val exSelectedUsers: List<User> = emptyList()
) : ViewModel() {

    @Suppress("UNCHECKED_CAST")
    class Factory(
        val miCore: MiCore,
        private val selectedUserIds: List<User.Id>?,
        val selectedUsers: List<User>?
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SelectedUserViewModel(
                miCore,
                selectedUserIds ?: emptyList(),
                selectedUsers ?: emptyList()
            ) as T
        }
    }


    data class ChangedDiffResult(
        val selected: List<User.Id>,
        val added: List<User.Id>,
        val removed: List<User.Id>,
        val selectedUserNames: List<String>,
    ) : Serializable

    private val _state = MutableStateFlow(
        SelectedUserUiState(
            exSelectedUserIds.toSet()
        )
    )

    val selectedUserIds = _state.map {
        it.selectedUserIds
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    val selectedUserList = selectedUserIds.flatMapLatest { ids ->
        miCore.getUserDataSource().state.map { state ->
            ids.mapNotNull {
                state.get(it)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    fun toggleSelectUser(user: User?) {
        user ?: return

        _state.update { state ->
            state.toggle(user.id)
        }

    }


    fun getSelectedUserIdsChangedDiff(): ChangedDiffResult {
        val selectedBeforeIds = exSelectedUserIds.toSet()
        val selectedBeforeUsers = exSelectedUsers.map {
            it.id
        }.toSet()
        val exSelected = HashSet<User.Id>().apply {
            addAll(selectedBeforeIds)
            addAll(selectedBeforeUsers)
        }

        val selected = _state.value.selectedUserIds

        val selectedUsers = selectedUserList.value


        val added = selected.filter { s ->
            !exSelected.contains(s)
        }

        val removed = exSelected.filter { ex ->
            !selected.contains(ex)
        }
        return ChangedDiffResult(
            selected.toList().toList(),
            added,
            removed,
            selectedUsers.map {
                it.displayUserName
            })
    }
}