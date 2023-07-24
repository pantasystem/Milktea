package net.pantasystem.milktea.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.asLoadingStateFlow
import net.pantasystem.milktea.common.initialState
import net.pantasystem.milktea.model.group.Group
import net.pantasystem.milktea.model.group.GroupDataSource
import net.pantasystem.milktea.model.group.GroupRepository
import net.pantasystem.milktea.model.group.GroupWithMember
import net.pantasystem.milktea.model.user.UserRepository
import java.util.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class GroupListViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val accountStore: AccountStore,
    private val groupDataSource: GroupDataSource,
    private val userRepository: UserRepository,
    loggerFactory: Logger.Factory,
) : ViewModel() {

    private val logger = loggerFactory.create("GroupViewModel")

    @OptIn(ExperimentalCoroutinesApi::class)
    private val joinedGroups = accountStore.observeCurrentAccount.filterNotNull().flatMapLatest { account ->
        groupDataSource.observeJoinedGroups(account.accountId)
    }.flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val ownedGroups = accountStore.observeCurrentAccount.filterNotNull().flatMapLatest { account ->
        groupDataSource.observeOwnedGroups(account.accountId)
    }.flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val syncEvents = MutableSharedFlow<UUID>(onBufferOverflow = BufferOverflow.DROP_OLDEST, extraBufferCapacity = 100)

    private val ownedGroupSyncState = syncEvents.flatMapLatest {
        accountStore.observeCurrentAccount
    }.filterNotNull().flatMapLatest {

        suspend {
            groupRepository.syncByOwned(it.accountId)
        }.asLoadingStateFlow()
    }.flowOn(Dispatchers.IO).stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        ResultState.initialState()
    )

    private val joinedGroupSyncState = syncEvents.flatMapLatest {
        accountStore.observeCurrentAccount
    }.filterNotNull().flatMapLatest {
        logger.debug { "joinedGroupSyncState" }
        suspend {
            groupRepository.syncByJoined(it.accountId)
        }.asLoadingStateFlow()
    }.flowOn(Dispatchers.IO).stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        ResultState.initialState(),
    )

    val uiState = combine(
        ownedGroups,
        joinedGroups,
        ownedGroupSyncState,
        joinedGroupSyncState
    ) { ownedGroups, joinedGroups, ownedSyncState, joinedSyncState ->
        GroupListUiState(
            ownedGroups = ownedGroups,
            joinedGroups = joinedGroups,
            syncJoinedGroupsState = joinedSyncState,
            syncOwnedGroupsState = ownedSyncState
        )
    }.flowOn(Dispatchers.IO).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(1000),
        GroupListUiState(
            emptyList(),
            emptyList(),
            ResultState.initialState(),
            ResultState.initialState()
        )
    )

    init {
        sync()
        combine(joinedGroups, ownedGroups) { joined, owned ->
            (joined.map {
                it.group.userIds
            }.flatten() + owned.map {
                it.group.userIds
            }.flatten()).distinct()
        }.distinctUntilChanged().map {
            userRepository.syncIn(it).onFailure { e ->
                logger.error("ユーザーの同期エラー", e)
            }
        }.launchIn(viewModelScope)
    }

    fun sync() {
        syncEvents.tryEmit(UUID.randomUUID())
    }

}

data class GroupListUiState(
    val joinedGroups: List<GroupWithMember>,
    val ownedGroups: List<GroupWithMember>,
    val syncJoinedGroupsState: ResultState<List<Group>>,
    val syncOwnedGroupsState: ResultState<List<Group>>,
)