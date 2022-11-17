package net.pantasystem.milktea.userlist.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
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

    val connection = remember {
        LazyInitialNestedScrollConnection()
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
            CollapsingTopAppBar(
                modifier = Modifier,
                onNavigateUp = { onAction(UserListCardScreenAction.OnNavigateUp) },
                scrollConnection = connection
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
        UserListList(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .nestedScroll(connection),
            uiState = uiState, onAction = onAction
        )
    }
}

@Composable
private fun CollapsingTopAppBar(
    modifier: Modifier,
    onNavigateUp: () -> Unit,
    scrollConnection: LazyInitialNestedScrollConnection,
) {

    var toolbarHeight by remember {
        mutableStateOf(56.dp)
    }


    val density = LocalDensity.current

    val defaultFontSize = MaterialTheme.typography.h6.fontSize

    var fontSize by remember {
        mutableStateOf(defaultFontSize)
    }

    LaunchedEffect(null) {
        scrollConnection.preScroll = { available, _ ->
            val delta = available.y
            toolbarHeight = with(density) {
                (toolbarHeight.toPx() + delta).coerceIn(0.dp.toPx(), 56.dp.toPx()).toDp()
            }
            fontSize = (defaultFontSize.value * (toolbarHeight / 56.dp)).sp

            Offset.Zero
        }
    }

    TopAppBar(
        title = {
            Text(
                text = stringResource(id = R.string.user_list),
                style = MaterialTheme.typography.h6.copy(fontSize = fontSize)
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateUp) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
            }
        },
        backgroundColor = MaterialTheme.colors.surface,
        modifier = modifier.height(toolbarHeight)
    )
}

@Composable
@Stable
fun UserListList(
    modifier: Modifier,
    uiState: UserListsUiState,
    onAction: (UserListCardScreenAction) -> Unit
) {
    LazyColumn(
        modifier = modifier,
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

private class LazyInitialNestedScrollConnection(
    var preScroll: (available: Offset, source: NestedScrollSource) -> Offset = { _, _ -> Offset.Zero }
) : NestedScrollConnection {
    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        return preScroll(available, source)
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