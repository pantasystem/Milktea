package net.pantasystem.milktea.group

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Message
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.pantasystem.milktea.model.group.Group


@Composable
fun GroupDetailPage(uiState: GroupDetailUiState, onAction: (GroupDetailPageAction) -> Unit,) {
    if (uiState.type is GroupDetailUiStateType.Editing) {
        GroupEditorDialog(
            uiState.type.groupId == null,
            uiState.type.name,
            onAction = { action ->
                when(action) {
                    is GroupEditorDialogAction.OnSave -> {
                        onAction(GroupDetailPageAction.OnConfirmedSave)
                    }
                    is GroupEditorDialogAction.OnDismiss -> {
                        if (uiState.type.groupId == null) {
                            onAction(GroupDetailPageAction.OnNavigateUp)
                        } else {
                            onAction(GroupDetailPageAction.OnEditingCanceled)
                        }
                    }
                    is GroupEditorDialogAction.OnNameChanged -> {
                        onAction(GroupDetailPageAction.OnInputName(action.text))
                    }
                }
            }
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        uiState.title,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onAction(GroupDetailPageAction.OnNavigateUp) }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                backgroundColor = MaterialTheme.colors.surface,
                actions = {
                    if (uiState.isOwner) {
                        IconButton(onClick = {
                            onAction(GroupDetailPageAction.OnEdit)
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }
                    }
                    IconButton(onClick = {
                        uiState.group?.let {
                            onAction(GroupDetailPageAction.ShowMessaging(it))
                        }

                    }) {
                        Icon(Icons.Default.Message, contentDescription = null)
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.isOwner) {
                FloatingActionButton(onClick = {
                    onAction(GroupDetailPageAction.OnInviteUsers(uiState.group!!))
                }) {
                    Icon(Icons.Default.GroupAdd, contentDescription = null)
                }
            }
        }
    ) {
        LazyColumn(Modifier.padding(it)) {
            items(uiState.members.size) { index ->
                val member = uiState.members[index]
                GroupMemberCard(
                    member = member,
                    ownerId = uiState.group?.ownerId,
                    isOwnGroup = uiState.isOwner,
                    onAction = { action ->
                        onAction(GroupDetailPageAction.OnMemberAction(action))
                    }
                )
            }
        }
    }
}

sealed interface GroupDetailPageAction {
    object OnNavigateUp : GroupDetailPageAction
    data class OnInputName(val text: String) : GroupDetailPageAction
    object OnConfirmedSave : GroupDetailPageAction
    object OnEditingCanceled : GroupDetailPageAction
    object OnEdit : GroupDetailPageAction
    data class OnMemberAction(val action: GroupMemberCardAction) : GroupDetailPageAction
    data class OnInviteUsers(val group: Group) : GroupDetailPageAction
    data class ShowMessaging(val group: Group) : GroupDetailPageAction
}