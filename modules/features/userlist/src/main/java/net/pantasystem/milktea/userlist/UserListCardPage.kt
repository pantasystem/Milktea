package net.pantasystem.milktea.userlist

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.pantasystem.milktea.model.list.UserList
import net.pantasystem.milktea.userlist.viewmodel.UserListsUiState

@Composable
fun UserListCardPage(
    uiState: UserListsUiState,
    onAction: (UserListCardPageAction) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.user_list))
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onAction(UserListCardPageAction.OnNavigateUp)
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
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
                                onAction(
                                    UserListCardPageAction
                                        .OnUserListCardClicked(
                                            uiState.userLists[index].userList.userList
                                        )
                                )
                            }
                            is UserListCardAction.OnClickToggleTab -> {
                                onAction(
                                    UserListCardPageAction.OnUserListAddToTabToggled(
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

sealed interface UserListCardPageAction {
    data class OnUserListCardClicked(val userList: UserList) : UserListCardPageAction
    data class OnUserListAddToTabToggled(val userList: UserList) : UserListCardPageAction
    object OnNavigateUp : UserListCardPageAction

}