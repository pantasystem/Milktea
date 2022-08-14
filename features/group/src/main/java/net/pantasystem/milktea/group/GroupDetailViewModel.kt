package net.pantasystem.milktea.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.asLoadingStateFlow
import net.pantasystem.milktea.model.group.Group
import net.pantasystem.milktea.model.group.GroupDataSource
import net.pantasystem.milktea.model.group.GroupRepository
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class GroupDetailViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userDataSource: UserDataSource,
    private val groupRepository: GroupRepository,
    private val groupDataSource: GroupDataSource,
): ViewModel() {
    private val uiStateType = MutableStateFlow<GroupDetailUiStateType>(GroupDetailUiStateType.Editing(null))

    val groupSyncState = uiStateType.mapNotNull { type ->
        type.groupId
    }.flatMapLatest {
        suspend {
            groupRepository.syncOne(it)
        }.asLoadingStateFlow()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ResultState.Loading(StateContent.NotExist()))

    val membersSyncState = uiStateType.mapNotNull { type ->
        type.groupId
    }.flatMapLatest {
        groupDataSource.observeOne(it)
    }.flatMapLatest {
        suspend {
            userRepository.syncIn(it.group.userIds).getOrThrow()
        }.asLoadingStateFlow()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ResultState.Loading(StateContent.NotExist()))

    val members = uiStateType.mapNotNull { type ->
        type.groupId
    }.flatMapLatest {
        groupDataSource.observeOne(it)
    }.flatMapLatest {
        userDataSource.observeIn(it.group.id.accountId, it.group.userIds.map { it.id })
    }

    val group = uiStateType.mapNotNull { type ->
        type.groupId
    }.flatMapLatest {
        groupDataSource.observeOne(it)
    }

    val uiState = combine(uiStateType, group, groupSyncState, membersSyncState, members) { type, g, gss, mss, m ->
        GroupDetailUiState(
            type,
            g.group,
            m,
            mss,
            gss,
        )
    }


    fun setState(stateType: GroupDetailUiStateType) {
        uiStateType.update {
            stateType
        }
    }
}

data class GroupDetailUiState(
    val type: GroupDetailUiStateType,
    val group: Group?,
    val members: List<User> = emptyList(),
    val syncMembersState: ResultState<List<User.Id>> = ResultState.Fixed(StateContent.NotExist()),
    val syncGroupState: ResultState<Group> = ResultState.Fixed(StateContent.NotExist()),
    val title: String = "",
)

sealed interface GroupDetailUiStateType {
    val groupId: Group.Id?
    data class Editing(override val groupId: Group.Id?, val name: String = "") : GroupDetailUiStateType {
        val isCreate: Boolean
            get() = groupId == null
    }
    data class Show(override val groupId: Group.Id) : GroupDetailUiStateType
}


