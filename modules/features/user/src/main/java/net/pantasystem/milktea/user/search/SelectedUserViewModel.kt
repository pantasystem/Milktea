package net.pantasystem.milktea.user.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common_navigation.ChangedDiffResult
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource

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
class SelectedUserViewModel @AssistedInject constructor(
    val userDataSource: UserDataSource,
    val loggerFactory: Logger.Factory,
    val accountStore: AccountStore,
    @Assisted val exSelectedUserIds: List<User.Id>,
    @Assisted val exSelectedUsers: List<User>,
) : ViewModel() {


    @AssistedFactory
    interface AssistedViewModelFactory {
        fun create(
            exSelectedUserIds: List<User.Id>,
            exSelectedUsers: List<User>
        ): SelectedUserViewModel
    }



    companion object;

    private val _state = MutableStateFlow(
        SelectedUserUiState(
            exSelectedUserIds.toSet()
        )
    )

    val selectedUserIds = _state.map {
        it.selectedUserIds
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    val selectedUserList = selectedUserIds.flatMapLatest { ids ->
        val accountId = ids.map { it.accountId }.distinct().firstOrNull()
            ?: accountStore.currentAccountId!!
        userDataSource.observeIn(accountId, ids.toList().map { it.id })
    }.catch {
        loggerFactory.create("SelectedUserVM").error("failed observe selected user list", it)
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

@Suppress("UNCHECKED_CAST")
fun SelectedUserViewModel.Companion.provideViewModel(
    factory: SelectedUserViewModel.AssistedViewModelFactory,
    selectedUserIds: List<User.Id>,
    selectedUsers: List<User>
) = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return factory.create(selectedUserIds, selectedUsers) as T
    }
}