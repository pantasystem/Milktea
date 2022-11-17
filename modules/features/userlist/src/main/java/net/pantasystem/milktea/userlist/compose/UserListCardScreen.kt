package net.pantasystem.milktea.userlist.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import net.pantasystem.milktea.model.list.UserList
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.userlist.R
import net.pantasystem.milktea.userlist.viewmodel.UserListsUiState

@Composable
fun UserListCardScreen(
    uiState: UserListsUiState,
    onAction: (UserListCardScreenAction) -> Unit,
) {
    var isShowCreateUserListDialog: Boolean by remember {
        mutableStateOf(false)
    }

    CreateUserListDialog(isShow = isShowCreateUserListDialog, onDismiss = {
        isShowCreateUserListDialog = false
    }, onSave = {
        isShowCreateUserListDialog = false
        onAction(UserListCardScreenAction.OnSaveNewUserList(it))
    })
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.user_list))
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onAction(UserListCardScreenAction.OnNavigateUp)
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                backgroundColor = MaterialTheme.colors.surface
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                isShowCreateUserListDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Create a User list")
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            items(uiState.userLists.size) { index ->
                UserListCard(
                    userList = uiState.userLists[index],
                    onAction = { action ->
                        when (action) {
                            is UserListCardAction.OnClick -> {
                                if (uiState.addTargetUserId == null) {
                                    onAction(
                                        UserListCardScreenAction
                                            .OnUserListCardClicked(
                                                uiState.userLists[index].userList.userList
                                            )
                                    )
                                } else {
                                    onAction(
                                        UserListCardScreenAction
                                            .OnToggleAddUser(
                                                uiState.userLists[index].userList.userList,
                                                uiState.addTargetUserId
                                            )
                                    )
                                }

                            }
                            is UserListCardAction.OnClickToggleTab -> {
                                onAction(
                                    UserListCardScreenAction.OnUserListAddToTabToggled(
                                        uiState.userLists[index].userList.userList
                                    )
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}


@Composable
fun CreateUserListDialog(
    isShow: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {

    var name: String by remember(isShow) {
        mutableStateOf("")
    }

    if (isShow) {
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colors.surface,
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        stringResource(R.string.create_user_list),
                        fontSize = 24.sp,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = name,
                        placeholder = {
                            Text(stringResource(R.string.list_name))
                        },
                        onValueChange = { text ->
                            name = text
                        }
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(stringResource(R.string.cancel))
                        }
                        TextButton(onClick = {
                            onSave.invoke(name)
                        }) {
                            Text(stringResource(R.string.save))
                        }
                    }
                }
            }
        }
    }

}

sealed interface UserListCardScreenAction {
    data class OnUserListCardClicked(val userList: UserList) : UserListCardScreenAction
    data class OnUserListAddToTabToggled(val userList: UserList) : UserListCardScreenAction
    object OnNavigateUp : UserListCardScreenAction

    data class OnToggleAddUser(val userList: UserList, val userId: User.Id) :
        UserListCardScreenAction

    data class OnSaveNewUserList(val name: String) : UserListCardScreenAction
}