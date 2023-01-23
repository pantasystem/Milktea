package net.pantasystem.milktea.group

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import net.pantasystem.milktea.model.group.Group
import net.pantasystem.milktea.model.user.User

@Composable
fun GroupDetailStatePage(
    groupDetailViewModel: GroupDetailViewModel,
    onAction: (GroupDetailStatePageAction) -> Unit,
) {
    val detailUiState by groupDetailViewModel.uiState.collectAsState()
    GroupDetailPage(detailUiState, onAction = { action ->
        when(action) {
            GroupDetailPageAction.OnNavigateUp -> {
                groupDetailViewModel.cancelEditing()
                onAction(GroupDetailStatePageAction.PopBackStack)
            }
            is GroupDetailPageAction.OnInputName -> {
                groupDetailViewModel.setName(action.text)
            }
            GroupDetailPageAction.OnConfirmedSave -> {
                groupDetailViewModel.save()
            }
            GroupDetailPageAction.OnEditingCanceled -> {
                groupDetailViewModel.cancelEditing()
            }
            is GroupDetailPageAction.ShowMessaging -> {
                onAction(GroupDetailStatePageAction.OnShowMessage(action.group))
            }
            is GroupDetailPageAction.OnMemberAction -> {
                when(action.action) {
                    is GroupMemberCardAction.OnClick -> {
                        onAction(GroupDetailStatePageAction.OnShowUser(action.action.user))
                    }
                    is GroupMemberCardAction.RejectMember -> {

                    }
                }
            }
            is GroupDetailPageAction.OnInviteUsers -> {
                onAction(GroupDetailStatePageAction.OnInviteUsers(action.group))
            }
            is GroupDetailPageAction.OnEdit -> {
                groupDetailViewModel.setState(GroupDetailUiStateType.Editing(
                    detailUiState.type.groupId
                ))
            }
        }
    })
}

sealed interface GroupDetailStatePageAction {
    object PopBackStack : GroupDetailStatePageAction
    data class OnShowUser(val user: User) : GroupDetailStatePageAction
    data class OnInviteUsers(val group: Group) : GroupDetailStatePageAction
    data class OnShowMessage(val group: Group) : GroupDetailStatePageAction
}