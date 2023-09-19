package net.pantasystem.milktea.group

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.group.GroupWithMember
import net.pantasystem.milktea.model.user.User
import javax.inject.Inject

class GroupDetailUiStateBuilder @Inject constructor() {

    operator fun invoke(
        currentAccountFlow: Flow<Account?>,
        uiStateType: Flow<GroupDetailUiStateType>,
        groupFlow: Flow<GroupWithMember?>,
        syncStateFlow: Flow<SyncState>,
        membersFlow: Flow<List<User>>,
    ): Flow<GroupDetailUiState> {
        return combine(
            currentAccountFlow,
            uiStateType,
            groupFlow,
            syncStateFlow,
            membersFlow,
        ) { ac, type, g, sync,  m ->
            // NOTE: groupIdがnullの状態の時はグループ関連のデータを空かnullにしたい
            // NOTE: 源流となるflowがgroupIdがnullの時はfilterするようにしてしまっているので、ここでempty, nullを割り当てている
            GroupDetailUiState(
                ac,
                type,
                if (type.groupId == null) null else g?.group,
                if (type.groupId == null) emptyList() else m,
                if (type.groupId == null) ResultState.Fixed(StateContent.NotExist()) else sync.syncMembersState,
                if (type.groupId == null) ResultState.Fixed(StateContent.NotExist()) else sync.syncGroupState,
                when (type) {
                    is GroupDetailUiStateType.Editing -> {
                        type.name
                    }
                    is GroupDetailUiStateType.Show -> {
                        g?.group?.name ?: ""
                    }
                    is GroupDetailUiStateType.Rejecting -> {
                        g?.group?.name ?: ""
                    }
                }
            )
        }
    }
}